CREATE OR REPLACE PROCEDURE sp_setDefaultPageletNames (sourceLocaleID IN VARCHAR2, targetLocaleID IN VARCHAR2)
------------------------------------------------------------------------------------------
-- Name        : setDefaultPageletNames
-- History     : user                        date        - desc
--             : ...
--             : t.hofbeck@intershop.de     20/05/2014  - basic functionality
--				 t.hofbeck@intershop.de     16/11/2015  - extended by targetLocaleID
--
-- Created     : t.hofbeck@intershop.de
-- Description : This procedure sets pagelet names of target locale to the pagelet name of a given source locale.
--
-- Input       : none.
-- Output      : none.
-- Example     : exec sp_setDefaultPageletNames;
------------------------------------------------------------------------------------------
AS
  dName pagelet_av.name%TYPE := 'displayName';

BEGIN

  EXECUTE IMMEDIATE 'insert into pagelet_av (ownerid, name, localeid, localizedflag, type, stringvalue, oca, lastmodified)
                        select ownerid, name, (select localeId from localeinformation where localeId = :targetLocaleID), localizedFlag, type, stringvalue, 0, CURRENT_TIMESTAMP
                          from pagelet_av
                          where name=:dName
                          and localeid=:sourceLocaleID
                          and ownerid not in
                            (select ownerid from pagelet_av where localeid = :targetLocaleID)' USING IN targetLocaleID, dName, sourceLocaleID, targetLocaleID;

  COMMIT;


END sp_setDefaultPageletNames;
/
