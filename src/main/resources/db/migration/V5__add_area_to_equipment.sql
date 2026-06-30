-- Flyway migration to add 'area' column to the 'equipment' table
ALTER TABLE equipment ADD COLUMN area VARCHAR(100) NULL;

-- Update existing seeded equipment with appropriate area descriptions
UPDATE equipment SET area = 'Khu Cardio – Tầng 1' WHERE equipment_code = 'EQ001';
UPDATE equipment SET area = 'Khu Sức mạnh – Tầng 2' WHERE equipment_code = 'EQ002';
UPDATE equipment SET area = 'Khu Sức mạnh – Tầng 2' WHERE equipment_code = 'EQ003';
