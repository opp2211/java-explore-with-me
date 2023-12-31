DROP TABLE IF EXISTS endpoint_hit CASCADE;

CREATE TABLE IF NOT EXISTS endpoint_hit (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  app VARCHAR(64) NOT NULL,
  uri VARCHAR(255) NOT NULL,
  ip VARCHAR(15) NOT NULL,
  hit_datetime TIMESTAMP NOT NULL,
  CONSTRAINT pk_endpoint_hit PRIMARY KEY (id)
);