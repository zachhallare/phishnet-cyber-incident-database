CREATE DATABASE IF NOT EXISTS CybersecurityDB;
USE CybersecurityDB;

-- ==============================
-- PhishNet Database Schema
-- Version: 3.0 (Argon2 Integration)
-- ==============================
-- This file contains the database structure only.
-- For initial data inserts, see PhishNet-inserts.sql
-- ==============================

-- ==============================
-- TABLE: Victims
-- Purpose: Stores user accounts for incident reporters (victims of attacks)
-- ==============================
CREATE TABLE IF NOT EXISTS Victims (
    VictimID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    ContactEmail VARCHAR(100) UNIQUE NOT NULL,
    PasswordHash VARCHAR(255) NOT NULL,
    AccountStatus ENUM('Active', 'Flagged', 'Suspended') DEFAULT 'Active',
    DateCreated DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ==============================
-- TABLE: Perpetrators
-- Purpose: Stores threat actors and their identifiers (phone numbers, emails, etc.)
-- ==============================
CREATE TABLE IF NOT EXISTS Perpetrators (
    PerpetratorID INT AUTO_INCREMENT PRIMARY KEY,
    Identifier VARCHAR(255) UNIQUE NOT NULL,
    IdentifierType ENUM('Phone Number', 'Email Address', 'Social Media Account', 'Website URL', 'IP Address') NOT NULL,
    AssociatedName VARCHAR(100),
    ThreatLevel ENUM('UnderReview', 'Suspected', 'Malicious', 'Cleared') DEFAULT 'UnderReview',
    LastIncidentDate DATETIME
);

-- ==============================
-- TABLE: AttackTypes
-- Purpose: Categorizes different types of cyber attacks
-- ==============================
CREATE TABLE IF NOT EXISTS AttackTypes (
    AttackTypeID INT AUTO_INCREMENT PRIMARY KEY,
    AttackName VARCHAR(100) NOT NULL,
    Description TEXT,
    SeverityLevel ENUM('Low', 'Medium', 'High') DEFAULT 'Low'
);

-- ==============================
-- TABLE: Administrators
-- Purpose: Stores system administrators and cybersecurity staff accounts
-- ==============================
CREATE TABLE IF NOT EXISTS Administrators (
    AdminID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    Role ENUM('System Admin', 'Cybersecurity Staff') NOT NULL,
    ContactEmail VARCHAR(100) UNIQUE NOT NULL,
    PasswordHash VARCHAR(255) NOT NULL,
    DateAssigned DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ==============================
-- TABLE: IncidentReports
-- Purpose: Links victims, perpetrators, and attack types to create incident reports
-- ==============================
CREATE TABLE IF NOT EXISTS IncidentReports (
    IncidentID INT AUTO_INCREMENT PRIMARY KEY,
    VictimID INT NOT NULL,
    PerpetratorID INT NOT NULL,
    AttackTypeID INT NOT NULL,
    AdminID INT,
    DateReported DATETIME DEFAULT CURRENT_TIMESTAMP,
    Description TEXT,
    Status ENUM('Pending', 'Validated', 'Rejected') DEFAULT 'Pending',
    FOREIGN KEY (VictimID) REFERENCES Victims(VictimID) ON DELETE CASCADE,
    FOREIGN KEY (PerpetratorID) REFERENCES Perpetrators(PerpetratorID) ON DELETE CASCADE,
    FOREIGN KEY (AttackTypeID) REFERENCES AttackTypes(AttackTypeID),
    FOREIGN KEY (AdminID) REFERENCES Administrators(AdminID)
);

-- ==============================
-- TABLE: EvidenceUpload
-- Purpose: Stores evidence files (screenshots, emails, etc.) associated with incidents
-- ==============================
CREATE TABLE IF NOT EXISTS EvidenceUpload (
    EvidenceID INT AUTO_INCREMENT PRIMARY KEY,
    IncidentID INT NOT NULL,
    EvidenceType ENUM('Screenshot', 'Email', 'File', 'Chat Log') NOT NULL,
    FilePath VARCHAR(255) NOT NULL,
    SubmissionDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    VerifiedStatus ENUM('Pending', 'Verified', 'Rejected') DEFAULT 'Pending',
    AdminID INT,
    FOREIGN KEY (IncidentID) REFERENCES IncidentReports(IncidentID) ON DELETE CASCADE,
    FOREIGN KEY (AdminID) REFERENCES Administrators(AdminID)
);

-- ==============================
-- TABLE: RecycleBinReports
-- Purpose: Archives rejected incident reports for audit/recovery
-- ==============================
CREATE TABLE IF NOT EXISTS RecycleBinReports (
    BinID INT AUTO_INCREMENT PRIMARY KEY,
    IncidentID INT NOT NULL,
    VictimID INT,
    PerpetratorID INT,
    AttackTypeID INT,
    DateReported DATETIME,
    Description TEXT,
    OriginalStatus VARCHAR(50),
    AdminAssignedID INT,
    RejectedByAdminID INT NOT NULL,
    ArchiveReason VARCHAR(255),
    ArchivedAt DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ==============================
-- TABLE: RecycleBinEvidence
-- Purpose: Archives rejected evidence submissions for audit/recovery
-- ==============================
CREATE TABLE IF NOT EXISTS RecycleBinEvidence (
    BinID INT AUTO_INCREMENT PRIMARY KEY,
    EvidenceID INT NOT NULL,
    IncidentID INT,
    EvidenceType VARCHAR(50),
    FilePath VARCHAR(255),
    SubmissionDate DATETIME,
    OriginalStatus VARCHAR(50),
    AdminAssignedID INT,
    RejectedByAdminID INT NOT NULL,
    ArchiveReason VARCHAR(255),
    ArchivedAt DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ==============================
-- TABLE: ThreatLevelLog
-- Purpose: Tracks changes to perpetrator threat levels for audit purposes
-- ==============================
CREATE TABLE IF NOT EXISTS ThreatLevelLog (
    LogID INT AUTO_INCREMENT PRIMARY KEY,
    PerpetratorID INT NOT NULL,
    OldThreatLevel ENUM('UnderReview', 'Suspected', 'Malicious', 'Cleared'),
    NewThreatLevel ENUM('UnderReview', 'Suspected', 'Malicious', 'Cleared'),
    ChangeDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    AdminID INT,
    FOREIGN KEY (PerpetratorID) REFERENCES Perpetrators(PerpetratorID) ON DELETE CASCADE,
    FOREIGN KEY (AdminID) REFERENCES Administrators(AdminID)
);

-- ==============================
-- TABLE: VictimStatusLog
-- Purpose: Tracks changes to victim account status for audit purposes
-- ==============================
CREATE TABLE IF NOT EXISTS VictimStatusLog (
    LogID INT AUTO_INCREMENT PRIMARY KEY,
    VictimID INT NOT NULL,
    OldStatus ENUM('Active', 'Flagged', 'Suspended'),
    NewStatus ENUM('Active', 'Flagged', 'Suspended'),
    ChangeDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    AdminID INT,
    FOREIGN KEY (VictimID) REFERENCES Victims(VictimID) ON DELETE CASCADE,
    FOREIGN KEY (AdminID) REFERENCES Administrators(AdminID)
);

-- ==============================
-- Setup Instructions:
-- 1. Run this file (PhishNet-structure.sql) first to create the database schema
-- 2. Then run PhishNet-inserts.sql to populate initial data
-- ==============================

