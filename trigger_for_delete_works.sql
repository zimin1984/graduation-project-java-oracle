create or replace trigger trigger_for_delete_works
  after delete on works for each row
begin 
  delete from tasks where work_id = :old.work_id;
end trigger_for_delete_works;
/
