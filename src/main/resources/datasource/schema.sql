-- Create table: attributes
CREATE TABLE attributes (
    attribute_id BIGINT NOT NULL AUTO_INCREMENT,
    attribute_name VARCHAR(255) NOT NULL,
    PRIMARY KEY (attribute_id)
) ENGINE=InnoDB;

-- Create table: entities
CREATE TABLE entities (
    entity_id BIGINT NOT NULL AUTO_INCREMENT,
    entity_name VARCHAR(255) NOT NULL,
    PRIMARY KEY (entity_id)
) ENGINE=InnoDB;

-- Create table: attribute_values
CREATE TABLE attribute_values (
    value_id BIGINT NOT NULL AUTO_INCREMENT,
    attribute_id BIGINT NOT NULL,
    entity_id BIGINT NOT NULL,
    attribute_value VARCHAR(255),
    PRIMARY KEY (value_id),
    CONSTRAINT FK_attribute FOREIGN KEY (attribute_id) REFERENCES attributes(attribute_id),
    CONSTRAINT FK_entity FOREIGN KEY (entity_id) REFERENCES entities(entity_id)
) ENGINE=InnoDB;