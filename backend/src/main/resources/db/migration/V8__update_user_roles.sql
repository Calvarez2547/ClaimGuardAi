-- V8: Expand user roles to the documented role set.
-- The role column is VARCHAR(50) with no CHECK constraint, so adding new role values
-- requires only updating any seed data and the application enum.
-- Existing rows with USER/ADMIN values are migrated to the closest equivalent role.

UPDATE app_user_roles SET role = 'REVENUE_CYCLE_ANALYST' WHERE role = 'USER';
UPDATE app_user_roles SET role = 'ADMINISTRATOR' WHERE role = 'ADMIN';
