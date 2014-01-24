CREATE TABLE Organisation (
	OrganisationPID BIGINT IDENTITY NOT NULL PRIMARY KEY,

	Nummer VARCHAR(20) NOT NULL,
	Navn VARCHAR(60),
	Organisationstype VARCHAR(30) NOT NULL,

	CreatedDate DATETIME NOT NULL,
	ModifiedDate DATETIME NOT NULL,
	ValidFrom DATETIME NOT NULL,
	ValidTo DATETIME NOT NULL,

	INDEX (OrganisationPID, ModifiedDate),
	INDEX (Nummer, ValidTo, ValidFrom)
)

CREATE TABLE IF NOT EXISTS FGRImporterStatus (
    Id BIGINT IDENTITY NOT NULL PRIMARY KEY,
    Type VARCHAR(20),
    StartTime DATETIME NOT NULL,
    EndTime DATETIME,
    Outcome VARCHAR(20),
    ErrorMessage VARCHAR(200),

    INDEX (StartTime)
)

