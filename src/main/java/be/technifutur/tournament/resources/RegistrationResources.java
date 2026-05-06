//package be.technifutur.tournament.resources;
//
//import be.technifutur.tournament.daos.RegistrationDAO;
//import be.technifutur.tournament.entities.Registration;
//import jakarta.inject.Inject;
//import jakarta.ws.rs.*;
//import jakarta.ws.rs.core.Response;
//
//import java.util.List;
//
//@Path("/fighterResource")
//public class RegistrationResources {
//
//    @Inject
//    private RegistrationDAO registrationDAO;
//
//
//    @GET
//    @Produces("application/json")
//    public Response getAllRegistrations(){
//        List<Registration> allRegistrations = registrationDAO.findAll();
//
//        return Response.ok()
//                .entity(allRegistrations)
//                .build();
//    }
//
//    @GET
//    @Path("/id/{id}")
//    @Produces("application/json")
//    public Response findByName(@PathParam("id") Integer id){
//        Registration fighter = registrationDAO.findById(id).orElseThrow();
//
//        return Response.ok().entity(fighter).build();
//    }
//
//
//}
