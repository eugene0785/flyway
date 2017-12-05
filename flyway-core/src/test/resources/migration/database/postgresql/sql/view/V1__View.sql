--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--
-- Must
-- be
-- exactly
-- 13 lines
-- to match
-- community
-- edition
-- license
-- length.
--

CREATE TABLE """t""" (qty INT, price INT);
INSERT INTO """t""" VALUES(3, 50);
CREATE VIEW """v""" AS SELECT qty, price, qty*price AS value FROM """t""";

CREATE VIEW features AS SELECT * FROM information_schema.sql_features;