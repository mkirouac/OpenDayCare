CREATE USER 'flyway'@'%' IDENTIFIED BY 'f1ywayP@$sword2021';
GRANT ALL ON opendaycare_repo.* TO 'flyway'@'%';

CREATE USER 'application'@'%' IDENTIFIED BY '@pp1icati0nP@$sword2021';
GRANT SELECT, INSERT, UPDATE, DELETE ON opendaycare_repo.* TO 'application'@'%';

CREATE USER 'reporting'@'%' IDENTIFIED BY 'Rep0rt1ngP@$sword2021';
GRANT SELECT ON opendaycare_repo.* TO 'reporting'@'%';
