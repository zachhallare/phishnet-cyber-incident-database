USE CybersecurityDB;

-- ==============================
-- PhishNet Initial Data Inserts
-- Version: 3.0 (Argon2 Integration)
-- ==============================
-- This file contains initial administrator data with Argon2 password hashes.
-- 
-- IMPORTANT: Run PhishNet-structure.sql FIRST before running this file.
-- ==============================

-- ==============================
-- INSERT ADMINISTRATORS
-- Password for all administrators: PhishNetAdmin124
-- Password hash: Argon2id hash (generated with parameters: m=65536, t=3, p=4)
-- ==============================
-- 
-- Hash generated using Argon2id with SecurityUtils parameters
-- All administrators use the same password for initial setup
-- ==============================

INSERT INTO Administrators (Name, Role, ContactEmail, PasswordHash)
VALUES
('System Administrator', 'System Admin', 'admin@phishnet.com',
 '$argon2id$v=19$m=65536,t=3,p=4$ZWgiDa9T95Bv2X+maeqSEQ$gmrWCU6zCEjJZ6Scgs+tX26VESOa+0MJ6qxOLntc+ys'),

('Zach Benedict Hallare', 'Cybersecurity Staff', 'zach_benedict_hallare@dlsu.edu.ph',
 '$argon2id$v=19$m=65536,t=3,p=4$ZWgiDa9T95Bv2X+maeqSEQ$gmrWCU6zCEjJZ6Scgs+tX26VESOa+0MJ6qxOLntc+ys'),

('Benette Enzo Campo', 'Cybersecurity Staff', 'benette_campo@dlsu.edu.ph',
 '$argon2id$v=19$m=65536,t=3,p=4$ZWgiDa9T95Bv2X+maeqSEQ$gmrWCU6zCEjJZ6Scgs+tX26VESOa+0MJ6qxOLntc+ys'),

('Brent Prose Rebollos', 'Cybersecurity Staff', 'brent_rebollos@dlsu.edu.ph',
 '$argon2id$v=19$m=65536,t=3,p=4$ZWgiDa9T95Bv2X+maeqSEQ$gmrWCU6zCEjJZ6Scgs+tX26VESOa+0MJ6qxOLntc+ys'),

('Georgina Karylle Ravelo', 'Cybersecurity Staff', 'georgina_ravelo@dlsu.edu.ph',
 '$argon2id$v=19$m=65536,t=3,p=4$ZWgiDa9T95Bv2X+maeqSEQ$gmrWCU6zCEjJZ6Scgs+tX26VESOa+0MJ6qxOLntc+ys')
ON DUPLICATE KEY UPDATE Name = Name;

-- ==============================
-- INSERT ATTACK TYPES
-- Common cybersecurity attack categories
-- ==============================

INSERT INTO AttackTypes (AttackName, Description, SeverityLevel)
VALUES
('Phishing', 'Fraudulent attempt to obtain sensitive information by disguising as a trustworthy entity', 'High'),
('SMS Phishing (Smishing)', 'Phishing attacks conducted via SMS text messages', 'High'),
('Email Spoofing', 'Forged email headers to make messages appear from a different sender', 'Medium'),
('Social Engineering', 'Manipulation technique to trick users into revealing confidential information', 'High'),
('Malware Distribution', 'Distribution of malicious software through various channels', 'High'),
('Fake Website', 'Fraudulent website designed to mimic legitimate sites to steal credentials', 'High'),
('Vishing', 'Phishing attacks conducted via voice calls', 'Medium'),
('Credential Theft', 'Unauthorized acquisition of user credentials', 'High'),
('Account Takeover', 'Unauthorized access and control of user accounts', 'High'),
('Data Breach', 'Unauthorized access to confidential data', 'High')
ON DUPLICATE KEY UPDATE AttackName = AttackName;

-- ==============================
-- INSERT PERPETRATORS
-- Sample threat actors and identifiers
-- ==============================

INSERT INTO Perpetrators (Identifier, IdentifierType, AssociatedName, ThreatLevel, LastIncidentDate)
VALUES
('+63-912-345-6789', 'Phone Number', 'Unknown Scammer', 'Malicious', '2024-01-15 10:30:00'),
('scammer@fakebank.com', 'Email Address', 'Phishing Group Alpha', 'Malicious', '2024-01-20 14:22:00'),
('fake-dlsu-login.com', 'Website URL', 'Credential Harvesting Site', 'Malicious', '2024-02-01 09:15:00'),
('@suspicious_account', 'Social Media Account', 'Social Engineering Bot', 'Suspected', '2024-02-10 16:45:00'),
('192.168.1.100', 'IP Address', 'Unknown Attacker', 'UnderReview', '2024-02-15 11:20:00'),
('+63-998-765-4321', 'Phone Number', 'Vishing Operator', 'Malicious', '2024-02-20 13:10:00'),
('phishing@malicious-domain.net', 'Email Address', 'Phishing Campaign', 'Malicious', '2024-03-01 08:30:00'),
('fake-payment-gateway.com', 'Website URL', 'Payment Fraud Ring', 'Malicious', '2024-03-05 15:00:00')
ON DUPLICATE KEY UPDATE Identifier = Identifier;

-- ==============================
-- INSERT VICTIMS (User Registrations)
-- Sample user accounts for incident reporting
-- Password for all test victims: TestUser123
-- ==============================

INSERT INTO Victims (Name, ContactEmail, PasswordHash, AccountStatus)
VALUES
('Tester Cularcancer', 'tester@example.com',
 '$argon2id$v=19$m=65536,t=3,p=4$ZWgiDa9T95Bv2X+maeqSEQ$gmrWCU6zCEjJZ6Scgs+tX26VESOa+0MJ6qxOLntc+ys', 'Active'),

('Maria Santos', 'maria.santos@example.com',
 '$argon2id$v=19$m=65536,t=3,p=4$0fesm8msEaOZb7vwiqg0pQ$iadfO42ZgO4dM51XtywP4tiVvfjN+2QnyZvvMfrBAoA', 'Active'),

('Carlos Reyes', 'carlos.reyes@example.com',
 '$argon2id$v=19$m=65536,t=3,p=4$0fesm8msEaOZb7vwiqg0pQ$iadfO42ZgO4dM51XtywP4tiVvfjN+2QnyZvvMfrBAoA', 'Active'),

('Ana Garcia', 'ana.garcia@example.com',
 '$argon2id$v=19$m=65536,t=3,p=4$0fesm8msEaOZb7vwiqg0pQ$iadfO42ZgO4dM51XtywP4tiVvfjN+2QnyZvvMfrBAoA', 'Active'),

('Roberto Tan', 'roberto.tan@example.com',
 '$argon2id$v=19$m=65536,t=3,p=4$0fesm8msEaOZb7vwiqg0pQ$iadfO42ZgO4dM51XtywP4tiVvfjN+2QnyZvvMfrBAoA', 'Flagged'),

('Lisa Chen', 'lisa.chen@example.com',
 '$argon2id$v=19$m=65536,t=3,p=4$0fesm8msEaOZb7vwiqg0pQ$iadfO42ZgO4dM51XtywP4tiVvfjN+2QnyZvvMfrBAoA', 'Active')
ON DUPLICATE KEY UPDATE Name = Name;

-- ==============================
-- INSERT INCIDENT REPORTS
-- Sample incident reports linking victims, perpetrators, and attack types
-- ==============================
-- Note: AdminID is set to NULL initially (reports are pending validation)
-- AdminID will be assigned when an administrator validates the report

INSERT INTO IncidentReports (VictimID, PerpetratorID, AttackTypeID, AdminID, DateReported, Description, Status)
VALUES
(1, 1, 1, NULL, '2024-01-15 10:30:00', 
 'Received suspicious SMS claiming to be from bank asking for OTP. Number: +63-912-345-6789', 'Pending'),

(2, 2, 1, 1, '2024-01-20 14:22:00',
 'Phishing email received from scammer@fakebank.com asking to verify account details. Email contained suspicious links.', 'Validated'),

(3, 3, 6, NULL, '2024-02-01 09:15:00',
 'Discovered fake website fake-dlsu-login.com that looks identical to official DLSU portal. Attempted credential theft.', 'Pending'),

(4, 4, 4, 2, '2024-02-10 16:45:00',
 'Social media account @suspicious_account sent friend request and then asked for personal information.', 'Validated'),

(1, 5, 1, NULL, '2024-02-15 11:20:00',
 'Received phishing email from IP 192.168.1.100. Email contained malware attachment.', 'Pending'),

(5, 6, 7, NULL, '2024-02-20 13:10:00',
 'Received phone call from +63-998-765-4321 claiming to be from IT support. Caller asked for password reset.', 'Pending'),

(6, 7, 1, 3, '2024-03-01 08:30:00',
 'Phishing email from phishing@malicious-domain.net attempting to steal credentials through fake login page.', 'Validated'),

(2, 8, 6, NULL, '2024-03-05 15:00:00',
 'Encountered fake payment gateway website fake-payment-gateway.com during online transaction. Site requested full credit card details.', 'Pending')
ON DUPLICATE KEY UPDATE Description = Description;

