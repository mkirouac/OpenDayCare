

DELIMITER $$
CREATE DEFINER=`flyway`@`%` PROCEDURE `deleteKid`(IN idKidToDelete bigint)
BEGIN

  
    
	DECLARE idPersonToDelete BIGINT;
	  DECLARE EXIT HANDLER FOR SQLEXCEPTION
		BEGIN
			ROLLBACK;
		END;
   
  SELECT idPerson INTO idPersonToDelete FROM Kid k WHERE k.idKid = idKidToDelete;
	
    START TRANSACTION;
    
    DELETE FROM Family WHERE idKid = idKidToDelete;
	DELETE FROM Kid WHERE idKid = idKidToDelete;
    DELETE FROM DayCareGroupMember WHERE idPerson = idPersonToDelete;
	DELETE FROM Person WHERE idPerson = idPersonToDelete;
    
	COMMIT;
    
END$$

DELIMITER ;
;

