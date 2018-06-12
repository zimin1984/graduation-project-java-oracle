create or replace trigger trigger_for_delete_departments
  after delete on departments for each row
begin 
  delete from workers where department_id = :old.department_id;
  delete from works where department_id = :old.department_id;
end trigger_for_delete_departments;
/
