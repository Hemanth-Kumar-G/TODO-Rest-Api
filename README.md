# TODO-Rest-Api

  This Api is used for Creating , Update,Delete User.
   For a Particular user , Todo can be created, updated, deleted ,delete ALL todo's of particular User

    /** 
    * =====================================
    * ============USER====================
    * ====================================
    */
   
   POST "/v1/create"   PARAM= name,email,password   
   POST "/v1/login"   PARAM= email,password   
   POST "/v1/user"     need to send JWT token which you would be getting during create /login   
   PUT "/v1/user"   PARAM= name,email,password    need to send JWT token which you would be getting during create /login    
   PATCH "/v1/user"   ANY PARAM= name,email,password    need to send JWT token which you would be getting during create /login    
  
    /**
    * =====================================
    * ============TODO====================
    * ====================================
    */
    JWT token is must for authentication
    
     POST "/v1/todos"   PARAM= todo,done:Boolean
     GET "/v1/todos" 
     DELETE "/v1/todos/{id}"   deleted particular todo row
     DELETE "/v1/todos"    deletes all todo of user
     PUT "/v1/todos/{id}"  modify particular todo row
