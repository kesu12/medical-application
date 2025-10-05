ALTER TABLE users ADD COLUMN assigned_nurse_id BIGINT;
ALTER TABLE users ADD COLUMN assigned_doctor_id BIGINT;

ALTER TABLE users ADD CONSTRAINT fk_users_assigned_nurse 
    FOREIGN KEY (assigned_nurse_id) REFERENCES users(user_id);

ALTER TABLE users ADD CONSTRAINT fk_users_assigned_doctor 
    FOREIGN KEY (assigned_doctor_id) REFERENCES users(user_id);

CREATE INDEX idx_users_assigned_nurse ON users(assigned_nurse_id);
CREATE INDEX idx_users_assigned_doctor ON users(assigned_doctor_id);
CREATE INDEX idx_users_department_role ON users(department_id, role);