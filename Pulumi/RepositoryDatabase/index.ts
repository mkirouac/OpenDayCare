/*
* Deployment definition for the Repository Database
*/
import * as k8s from "@pulumi/kubernetes";
import { ConfigMap, LimitRangeList } from "@pulumi/kubernetes/core/v1";
import { Service } from "@pulumi/kubernetes/core/v1";
import { Deployment } from "@pulumi/kubernetes/apps/v1";
import { Job } from "@pulumi/kubernetes/batch/v1";
import * as fs from 'fs';

const appLabels = { app: "mysql" };

const FLYWAY_SCRIPTS_DIRECTORY = "scripts/schema";

/*
* VM used for debugging purposes.
*/ 
const debuggingVm = new k8s.apps.v1.Deployment("debuggingvm", {
    metadata:{name: "debuggingvm"},
    spec: {
        selector: { matchLabels: { app: "debuggingvm" } },
        replicas: 1,
		template: {
			metadata: { labels: { app: "debuggingvm" } },
			spec: {
				containers: [{
							name: "debuggingvm",
							image: "praqma/network-multitool",

							
				}],
			}
		}
    }
});

//init.db config-map
/*
* init.db config-map. Defines the scripts which will be executed upon database creation.
*/
const initdbConfigMap = new k8s.core.v1.ConfigMap("mysql-initdb-config", {
	metadata:{
		name: "mysql-initdb-config",
    },
    //TODO Use file
	data: {
		"initdb.sql": fs.readFileSync('scripts/init/initdb.sql').toString()
	},
});

/*
* Persistent volume to store the database files
*/
const pv = new k8s.core.v1.PersistentVolume("mysql-persistent-volume", {
	apiVersion: "v1",
	kind: "PersistentVolume",
	metadata: {
		name: "mysql-persistent-volume",
	},
	spec: {
		storageClassName: "manual",
		capacity: { storage: "1Gi" },
        accessModes: ["ReadWriteOnce"],
        // To delete data: 
        // # docker container run --rm -it -v /:/host alpine
        // # chroot /host
        // (now inside docker-desktop vm)
        // then delete /mnt/data
        // # rm -r /mnt/data
		hostPath: {path: "/mnt/data"}
	}
});

/*
* Persistent volume to store the database backups. It's accessible by both MySql database and the backup cron job.
*/
const pvBackups = new k8s.core.v1.PersistentVolume("mysql-persistent-volume-backups", {
	apiVersion: "v1",
	kind: "PersistentVolume",
	metadata: {
		name: "mysql-persistent-volume-backups",
	},
	spec: {
		storageClassName: "manual",
		capacity: { storage: "1Gi" },
        accessModes: ["ReadWriteOnce"],
        // To delete data: 
        // # docker container run --rm -it -v /:/host alpine
        // # chroot /host
        // (now inside docker-desktop vm)
        // then delete /mnt/backups
        // # rm -r /mnt/backups
		hostPath: {path: "/mnt/backups"}
	}
});

/*
* Persistent volume claim for mysql-persistent-volume 
*/
const pvc = new k8s.core.v1.PersistentVolumeClaim("mysql-persistent-volume-claim", {
	apiVersion: "v1",
	kind: "PersistentVolumeClaim",
	metadata: {
		name: "mysql-persistent-volume-claim"
	},
	spec: {
		storageClassName: "manual",//TODO Should this be removed? https://stackoverflow.com/questions/52668938/pod-has-unbound-persistentvolumeclaims (error with backup pvc)
		accessModes: ["ReadWriteOnce"],
		resources: { requests: { storage: "1Gi" } },
	},
});

/*
* Persistent volumn claim for mysql-persistent-volume-backups
*/
const pvcBackup = new k8s.core.v1.PersistentVolumeClaim("mysql-persistent-volume-claim-backups", {
	apiVersion: "v1",
	kind: "PersistentVolumeClaim",
	metadata: {
		name: "mysql-persistent-volume-claim-backups"
	},
	spec: {
		accessModes: ["ReadWriteMany"],
		resources: { requests: { storage: "1Gi" } },
	},
});


/*
* Deployment of mysql database 8.0.23
* TODO Ideally, use a Statefulset instead of a Deployment.
*/
const deployment = new k8s.apps.v1.Deployment("mysql", {
	spec: {
		selector: {matchLabels: appLabels},
		strategy: { type: "Recreate"},
		template: {
			metadata: { labels: { app: "mysql" } },
			spec: {
				containers:[{
                    image: "mysql:8.0.23",
					name: "mysql",
					env: [
                        {name: "MYSQL_ROOT_PASSWORD", value: "r0oTPa$sword2021"},//TODO Externalize
                        {name: "MYSQL_DATABASE", value: "opendaycare_repo"},
					],
					ports: [{containerPort: 3306, name: "mysql"}],
					volumeMounts: [
						{name: "mysql-persistent-storage", mountPath: "/var/lib/mysql"},
                        {name: "mysql-persistent-storage-backups", mountPath: "/var/lib/backups"},
						{name: "mysql-initdb", mountPath: "/docker-entrypoint-initdb.d"}
                    ],
				}],
				volumes: [
					{name: "mysql-persistent-storage", persistentVolumeClaim: {claimName: "mysql-persistent-volume-claim"}},
                    {name: "mysql-persistent-storage-backups", persistentVolumeClaim: {claimName: "mysql-persistent-volume-claim-backups"}},
					{name: "mysql-initdb", configMap: {name: "mysql-initdb-config"}}
				]
			},
		},
	},
});

/*
* MySql (Cluster IP)
*   To be used inside the cluster mysql:13306
*   Could eventually change the hostname and go back on default 3306 port
*/
const clusterIp = new Service("mysql-clusterIP", {
    metadata: {
        labels: deployment.spec.template.metadata.labels,
        name: "mysql",//hostname
    },
    spec: {
        type: "ClusterIP",
        ports: [{ port: 13306, targetPort: 3306, protocol: "TCP" }],
        selector: appLabels,

    }
}, { parent: deployment });

/*
* MySql (Load Balancer IP)
*   With docker desktop, this is used to access mysql from outside the cluster: localhost:13306
*/
const loadBalancerIp = new Service("mysql-loadbalancer", {
    metadata: {
        labels: deployment.spec.template.metadata.labels,
        name: "mysql-loadbalancer",
    },
    spec: {
        type: "LoadBalancer",
        ports: [{ port: 13306, targetPort: 3306, protocol: "TCP" }],
        selector: appLabels,

    }
}, { parent: deployment });

/*
* Flyway (SQL Scripts)
* Defines the scripts used for database schema and data migration.
*/

const flywayScriptsParent = new ConfigMap("flyway-scripts-parent", {}, { dependsOn: deployment });

const flywayScripts = createFlywaySqlScriptsConfigMaps(FLYWAY_SCRIPTS_DIRECTORY);


/*
* Flyway (job)
* A job that will be executed everytime a "pulumi up" command is issues and which will apply the latest migration scripts.
* If the database is already up to date, this job won't do anything.
*/
const flywayMigrationJob = new Job("flyway-job", {
    spec: {
        backoffLimit: 5,//fail after 5 minutes
        
        template: {
            spec: {
                containers: [{
                    name: "flyway-" + (Math.floor(Math.random() * 999) + 1),//The name will change every time, thus forcing this job to run along each "pulumi up". Any better way?
                    image: "flyway/flyway",
                    args: ["migrate", "-url=jdbc:mysql://mysql:13306/opendaycare_repo", "-user=flyway", "-password=f1ywayP@$sword2021"],//TODO Externalize password
                    volumeMounts: [{
                        name: "flyway-scripts", mountPath: "/flyway/sql", readOnly: true
                    },
                    ],
                }],
                initContainers:[{
                    name: "wait-for-mysql",
                    image: "busybox:latest",
                    //TODO mysql hostname
                    command: ["sh", "-c", "until nslookup mysql.$(cat /var/run/secrets/kubernetes.io/serviceaccount/namespace).svc.cluster.local; do echo waiting for mysql; sleep 2; done"]
                }],

                volumes: [
                    {
                        name: "flyway-scripts", projected: {
                            sources: projectFlywaySqlScriptsConfigMaps(flywayScripts)
                        }
                    }
                ],
                restartPolicy: "Never"
            }
        }
    }
}, {dependsOn: deployment})

/*
* Backup (job) 
* Cron job to backup the mysql database. It leverages the same mysql image as the database and executes a "mysqldump" command before exiting the pod.
*/
const backupCronJob = new k8s.batch.v1beta1.CronJob("backup-cron-job", {
    spec: {
        schedule: "	0 * * * *",//Hourly
        //schedule: "	* * * * *",//Every minute
        jobTemplate: {
            spec: {
                template: {
                    spec: {
                        restartPolicy:"Never",
                        containers: [{
                            name: "backup-cron-" + (Math.floor(Math.random() * 999) + 1),//The name will change every time, thus forcing this job to run along each "pulumi up". Any better way?
                            image: "mysql:8.0.23", 
                            command: ["sh", "-c", "mysqldump -h mysql --port 13306 -u backups --password='BacI<upsP@$sword2021' opendaycare_repo > /var/lib/backups/\"backup-$(date -u +\"%FT%H%MZ\").sql\"; exit;"],//TODO mysql hostname
                            
                            volumeMounts: [
                                {name: "mysql-persistent-storage-backups", mountPath: "/var/lib/backups"},
                            ],
                        }],
                        volumes: [
                            {name: "mysql-persistent-storage-backups", persistentVolumeClaim: {claimName: "mysql-persistent-volume-claim-backups"}},
                        ],
                    }
                }
            }
        }
    }
}, {dependsOn: deployment})

/*
* Functions
*/
function createFlywaySqlScriptsConfigMaps(dir: string): Array<ConfigMap> {

    let configMaps: Array<ConfigMap>;
    configMaps = [];

    const filesDir = fs.readdirSync(dir);
    let listofFiles: { [key: string]: string } = {};
    var i = 0;
    let lastConfigMap: ConfigMap = flywayScriptsParent;

    

    for (const file of filesDir) {
        let configMapName = "flyway-script-" + file.split("__")[0].replace(/_/g, "-").toLowerCase();//Underscore and upper cases are illegal as resource name.

        let currentConfigMap: ConfigMap = new ConfigMap(configMapName, {
            metadata: { name: configMapName,},
            immutable: true,
            data: createConfigMapData(dir, file),//Required as otherwise the filename will be "fileName" instead of the content of fileName const.
        }, { parent: flywayScriptsParent, dependsOn: lastConfigMap, deleteBeforeReplace: true });
        lastConfigMap = currentConfigMap;//Trick to keep the order of the scripts: every config map will dependsOn the previous one
        configMaps.push(currentConfigMap);
    }
    return configMaps;
}

function createConfigMapData(dir: string, file: string): { [key: string]: string } {
    //TODO How to do this without an array? The array makes it possible to dynamically specify a key based on the name of the file but there must be a better way.
    let listofFiles: { [key: string]: string } = {};
    const fileName = file.split("__")[0] + ".sql";//TODO Our script names are either too long or contain illegal characters such as "+"
    listofFiles[fileName] = fs.readFileSync(`${dir}\\${file}`).toString();
    return listofFiles;
}

function projectFlywaySqlScriptsConfigMaps(configMaps: Array<ConfigMap>): Array<k8s.types.input.core.v1.VolumeProjection> {

    let volumes: Array<k8s.types.input.core.v1.VolumeProjection> = [];

    for (const configMap of configMaps) {
        volumes.push({ configMap: { name: configMap.metadata.name } })
    }
    return volumes;
}

