import * as k8s from "@pulumi/kubernetes";
import { ConfigMap, LimitRangeList } from "@pulumi/kubernetes/core/v1";
import { Service } from "@pulumi/kubernetes/core/v1";
import { Deployment } from "@pulumi/kubernetes/apps/v1";
import { Job } from "@pulumi/kubernetes/batch/v1";
import * as fs from 'fs';

const appLabels = { app: "mysql" };

const FLYWAY_SCRIPTS_DIRECTORY = "scripts/schema";

/*
* VM for debugging
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
const initdbConfigMap = new k8s.core.v1.ConfigMap("mysql-initdb-config", {
	metadata:{
		name: "mysql-initdb-config",
    },
    //TODO Use file
	data: {
		"initdb.sql": fs.readFileSync('scripts/init/initdb.sql').toString()
	},
});

//persitent-volume
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

//persitent-volume for backups
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

//persistent-volume-claim
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

//persistent-volume-claim for backups
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
* Deployment
*/
//TODO Use statefulset instead
const deployment = new k8s.apps.v1.Deployment("mysql", {
	spec: {
		selector: {matchLabels: appLabels},
		strategy: { type: "Recreate"},
		template: {
			metadata: { labels: { app: "mysql" } },
			spec: {
				containers:[{
                    //image: "mysql:5.6",
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
                    /*
                    livenessProbe: {
                        exec: { command : ["mysqladmin", "ping"]},
                        initialDelaySeconds: 30,
                        periodSeconds: 10,
                        timeoutSeconds: 5,
                    },
                    */
                   /*
                    //mysql -h 127.0.0.1 -u root --password='r0oTPa$sword2021' -e 'SELECT 1'
                    readinessProbe: {
                        //exec: { command: ["mysql", "-h", "127.0.0.1", "-u", "root", "--password='r0oTPa$sword2021'", "-e", "'SELECT 1'"]  },//TODO Externalize password
                        //exec: { command: ["mysql -h 127.0.0.1 -u root --password='r0oTPa$sword2021' -e 'SELECT 1'"]  },//TODO Externalize password
                        tcpSocket: { port:3306 },
                        initialDelaySeconds: 5,
                        periodSeconds: 2,
                        timeoutSeconds: 1,
                    },
                    */
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
*/

const flywayScriptsParent = new ConfigMap("flyway-scripts-parent", {}, { dependsOn: deployment });

const flywayScripts = createFlywaySqlScriptsConfigMaps(FLYWAY_SCRIPTS_DIRECTORY);


/*
* Flyway (job)
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
*/

const backupJob = new Job("backup-job", {
    spec: {
        backoffLimit: 5,//fail after 5 minutes
        
        template: {
            spec: {
                containers: [{
                    name: "backup-" + (Math.floor(Math.random() * 999) + 1),//The name will change every time, thus forcing this job to run along each "pulumi up". Any better way?
                    image: "mysql:8.0.23", //WORKS but don't exit (obviously..)
                    //image: "sami/mysql-client",//caching_sha2 error
                    //image: "imega/mysql-client",//caching_sha2 error
                    //image: "arey/mysql-client",//caching_sha2 error
                    //image: "jcabillot/mysql-client",//write permission issue
                    //image: "baijunyao/mysql-client",//image doesnt exists
                    //image: "logiqx/mysql-client",
                    command: ["sh", "-c", "mysqldump -h mysql --port 13306 -u root --password='r0oTPa$sword2021' opendaycare_repo > /var/lib/backups/backup-$(date).sql; exit;"],//TODO mysql hostname
                    
					volumeMounts: [
                        {name: "mysql-persistent-storage-backups", mountPath: "/var/lib/backups"},
                    ],
                }],
				volumes: [
                    {name: "mysql-persistent-storage-backups", persistentVolumeClaim: {claimName: "mysql-persistent-volume-claim-backups"}},
				],
                restartPolicy: "Never"
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

