
CREATE  OR REPLACE VIEW EmployeeView AS
SELECT e.idEmployee, p.idPerson, e.employmentDate, p.firstName, p.lastName, p.dateOfBirth, p.registrationDate, p.updatedDate FROM Employee e INNER JOIN Person p on p.idPerson = e.idPerson;

CREATE OR REPLACE VIEW KidView AS
SELECT k.idKid, p.idPerson, p.firstName, p.lastName, p.dateOfBirth, p.registrationDate, p.updatedDate FROM Kid k INNER JOIN Person p on p.idPerson = k.idPerson;

CREATE OR REPLACE VIEW TutorView AS
SELECT t.idTutor, p.idPerson, p.firstName, p.lastName, p.dateOfBirth, p.registrationDate, p.updatedDate FROM Tutor t INNER JOIN Person p on p.idPerson = t.idPerson;

CREATE OR REPLACE VIEW FamilyView AS
SELECT 
	k.idKid as idKid,kp.firstName as kidFirstName, kp.lastName as kidLastName, kp.dateOfBirth as kidDateOfBirth,
    t.idTutor as idTutor, tp.firstName as tutorFirstName, tp.lastName as tutorLastName
    FROM Family f 
		INNER JOIN Kid k on k.idKid = f.idKid
		INNER JOIN Person kp on kp.idPerson = k.idPerson
		INNER JOIN Tutor t on t.idTutor = f.idTutor
		INNER JOIN Person tp on tp.idPerson = t.idPerson;

CREATE OR REPLACE VIEW SiblingView AS
SELECT  k.idKid AS idKid, k.firstName AS kidFirstName, k.lastName AS kidLastName
	, sibling_k.idKid AS siblingIdKid, sibling_k.firstName AS siblingFirstName, sibling_k.lastName AS siblingLastName FROM KidView k
	INNER JOIN Family direct_f on direct_f.idKid = k.idKid
    INNER JOIN Family siblings_f on siblings_f.idTutor = direct_f.idTutor AND siblings_f.idKid <> k.idKid
    INNER JOIN KidView sibling_k on sibling_k.idKid = siblings_f.idKid
    GROUP BY k.idKid, k.firstName, k.lastName, sibling_k.idKid, sibling_k.firstName, sibling_k.lastName
ORDER BY k.idKid;
    
CREATE OR REPLACE VIEW DayCareGroupView AS
SELECT  
	g.idDayCareGroup, g.groupName,
    p.firstName, p.lastName, 
    # In theory, a person could be both an employee and a kid. Think about the possibility to use DayCareGroupKids and DayCareGroupEmployees instead of DayCareGroupMember ?
    CASE
		WHEN idKid IS NOT NULL THEN 'Kid'
        WHEN idEmployee IS NOT NULL THEN 'Employee'
    END AS GroupMemberType,
    k.idKid,
    e.idEmployee
FROM DayCareGroup g
	INNER JOIN DayCareGroupMember gm on gm.idDayCareGroup = g.idDayCareGroup
    INNER JOIN Person p on p.idPerson = gm.idPerson
    LEFT JOIN Kid k on k.idPerson = p.idPerson
    LEFT JOIN Employee e on e.idPerson = p.idPerson;
    
    
CREATE OR REPLACE VIEW EmployeeSalaryView AS
SELECT e.idEmployee, p.firstName, p.lastName, es.hourlyRate, es.effectiveDate FROM Employee e
	INNER JOIN EmployeeSalary es on es.idEmployee = e.idEmployee
    INNER JOIN Person p on p.idPerson = e.idPerson
    ORDER BY e.idEmployee, es.hourlyRate;
    
CREATE OR REPLACE VIEW EmployeeSalaryRangeView AS 
#Assumption here that an employee didn't receive a pay decrease
SELECT idEmployee, firstName, lastName, MIN(hourlyRate) AS initialHourlyRate, MAX(hourlyRate) as currentHourlyRate FROM EmployeeSalaryView v_es 
	GROUP BY idEmployee, firstName, lastName;