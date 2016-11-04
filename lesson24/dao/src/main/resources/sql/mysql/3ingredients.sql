CREATE TABLE IF NOT EXISTS `ingredients` (
  id    INTEGER(10) UNSIGNED AUTO_INCREMENT NOT NULL,
  name  VARCHAR(250) NOT NULL ,

  PRIMARY KEY (id),
  CONSTRAINT eq_name UNIQUE (name)
);