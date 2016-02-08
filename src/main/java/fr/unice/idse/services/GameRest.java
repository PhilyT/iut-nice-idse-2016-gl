package fr.unice.idse.services;

import java.util.Arrays;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonAnyFormatVisitor;

import fr.unice.idse.constante.Config;
import fr.unice.idse.model.Deck;

import fr.unice.idse.model.Board;
import fr.unice.idse.model.Card;
import fr.unice.idse.model.Color;
import fr.unice.idse.model.Game;
import fr.unice.idse.model.Model;
import fr.unice.idse.model.Player;

/**
 * /game
 * │   ├── GET             Liste des parties (Fait)
 * │   ├── POST            Créer une partie (Fait)
 * │   ├── /{gamename}
 * │   │   ├── GET         Retourne l'état de la game (Fait)
 * │   │   ├── PUT         Ajoute un joueur dans la partie (Fait)
 * │   │   ├── DELETE      Supprime une partie
 * │   │   ├── /command
 * │   │   │   ├── GET     Retourne le joueur courant (Fait)
 * │   │   │   ├── PUT     Lance une partie (Que l'host) (Fait)
 * │   │   ├── /{pseudo}
 * │   │   │   ├── GET     Retoune la main du joueur (Fait)
 * │   │   │   ├── POST    Pioche une carte
 * │   │   │   ├── PUT     Joue une carte
 */

@Path("/game")
public class GameRest extends OriginRest{


    /**
     * Méthode permettant de lister toutes les parties existantes
     * Retour : {games : [
     *                      [name : String,
     *                       numberPlayers : String]
     *                   ]}
     * @return Response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listGame(){
        Model model = Model.getInstance();
        String [] list = new String[model.getGames().size()];
        for (int i = 0; i < model.getGames().size(); i++){
            list[i] = "{\"name\" : \""+model.getGames().get(i).getGameName()+"\", " +
                    "\"numberPlayers\" : \""+model.getGames().get(i).numberOfPlayers()+"/"+model.getGames().get(i).getNumberPlayers()+"\"}";
        }
        return sendResponse(200, "{\"games\" : "+ Arrays.toString(list)+"}", "GET");
    }

    /**
     * Méthode en POST permettant la création de partie.
     * Signature : {game: String, player: String(pseudo du joueur)}
     * Le nom de la game doit être suppérieur à 3 caractères;
     * Vérifie si la partie existe ou non. Renvoie {message: boolean}
     * @return Response
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGame(String objJSON) throws JSONException {
        // Cration de tous les objets
        Model model = Model.getInstance();
        JSONObject json = new JSONObject(objJSON);

        /*
        // verification du token
        if(!json.has("_token"))
            return Response.status(401).entity("{\"error\" : \"Invalid token\"}").build();
        if(!Config._token.equals(json.getString("_token")))
            return Response.status(401).entity("{\"error\" : \"Invalid token\"}").build();
         */
        
        // verification du champ game
        if(!json.has("game"))
            return sendResponse(405, "{\"error\" : \"Invalid parameter game\"}", "POST");
        String game = json.getString("game");
        if(game.length() < 3)
            return sendResponse(405, "{\"error\" : \"Invalid parameter game length\"}", "POST");
        if(!json.has("player"))
            return sendResponse(405, "{\"error\" : \"Invalid parameter player\"}", "POST");
        Player player = model.createPlayer(json.getString("player"), "");
        if(player == null)
            return sendResponse(405, "{\"error\" : \"Joueur existant\"", "POST");

        // creation de la game
        if(!model.addGame(player, game,4))
            return sendResponse(500, "{\"message\": false}", "POST");

        return sendResponse(200, "{\"message\": true}", "POST");
    }

    /**
     * Retourne si la partie a commencée
     * gamename : Nom de la partie
     *
     * Retourn {state: Boolean}
     * @param gamename Nom de partie
     * @return Response
     */
    @GET
    @Path("{gamename}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response isStarted(@PathParam("gamename") String gamename){
        Model model = Model.getInstance();
        Game game = model.findGameByName(gamename);

        if(game == null)
            return sendResponse(404, "Partie inconnu", "GET");

        return sendResponse(200, "{\"state\": "+game.getBoard().gameBegin()+"}", "GET");
    }

    /**
     * Méthode en POST permettant l'ajout d'un joueur dans une partie
     * Signature : {pseudo: String}
     * La partie doit être existante.
     * Renvoie {status: boolean}
     * @return Response
     */
    @PUT
    @Path("{gamename}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addPlayer(@PathParam("gamename") String gamename, String objJSON) throws JSONException{
        // Initialisation des objets
        Model model = Model.getInstance();
        Game game = model.findGameByName(gamename);
        JSONObject json = new JSONObject(objJSON);

        if(game == null)
            return sendResponse(404, "Partie inconnu", "PUT");

        // verification du token
        /*
        if(!json.has("_token"))
            return Response.status(401).entity("Invalid token").build();
        if(!Config._token.equals(json.getString("_token")))
            return Response.status(401).entity("Invalid token").build();
		*/
        
        // verification du joueur
        if(!json.has("pseudo"))
            return sendResponse(405, "Missing or invalid parameters", "PUT");
        Player player = model.createPlayer(json.getString("pseudo"),"");
        if(player == null)
            return sendResponse(405, "Missing or invalid parameters", "PUT");

        // verification game status
        if(game.gameBegin())
            return sendResponse(500, "Game started", "PUT");

        return sendResponse(200, "{\"status\" : "+model.addPlayerToGame(gamename, player)+"}", "PUT");
    }


    @PUT
    @Path("{gamename}/command")
    @Produces(MediaType.APPLICATION_JSON)
    public Response beginGame(@PathParam("gamename") String gamename, String objJSON) throws JSONException{
        // Initialisation des objets
        Model model = Model.getInstance();
        Game game = model.findGameByName(gamename);
        JSONObject json = new JSONObject(objJSON);

        if(game == null)
            return sendResponse(404, "Partie inconnu", "PUT");

        // verification du token
        /*
        if(!json.has("_token"))
            return Response.status(401).entity("Invalid token").build();
        if(!Config._token.equals(json.getString("_token")))
            return Response.status(401).entity("Invalid token").build();
		*/

        // verification du pseudo
        if(!json.has("pseudo"))
            return sendResponse(405, "Missing parameters pseudo", "PUT");
        if(model.findPlayerByName(gamename, json.getString("pseudo")) == null)
            return sendResponse(405, "Pseudo unknown", "PUT");

        if(model.findGameByName(gamename).gameBegin())
            return sendResponse(500, "Game started", "PUT");

        if(model.findGameByName(gamename).getNumberPlayers() == model.findGameByName(gamename).getBoard().getPlayers().size())
            if(model.startGame(gamename, json.getString("pseudo")))
                return sendResponse(200, "{\"status\": true}", "PUT");

        return sendResponse(500, "Game not tucked", "PUT");
    }
    
    /**
     * Méthode en GET permettant de recuperer le joueur devant jouer
     * La partie doit être existante.
     * Renvoie {"pseudo": String}
     * @return Response
     */
    @GET
    @Path("{gamename}/command")
    @Produces(MediaType.APPLICATION_JSON)
    public Response actualPlayer(@PathParam("gamename") String gamename) {
        // Initialisation des objets
        Model model = Model.getInstance();
        Game game = model.findGameByName(gamename);
        
        // Verifie si le jeu a commencer
    	if(!game.gameBegin())
            return sendResponse(401, "{\"error\":\"Game has not begin\"}", "GET");
    	
    	// Recherche le joueur actuel
    	Player currentPlayer = model.findGameByName(gamename).getBoard().getActualPlayer();

    	// Verifie qu'un joueur courant existe
    	if(currentPlayer == null)
            return sendResponse(422, "{\"error\":\"No current player has been set\"}", "GET");

        return sendResponse(200, "{\"pseudo\":\"" + currentPlayer.getName() + "\"}", "GET");
    }
    
    /*
     * @param playerName
     * @param gameName
     * @return
     * @throws JSONException 
     */
    @GET 
    @Path("/{gameName}/{pseudo}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response handplayer(@PathParam("pseudo") String pseudo,@PathParam("gameName") String gameName ) throws JSONException{
         Model model = Model.getInstance();
         Player player = model.findPlayerByName(gameName, pseudo);
         if(player==null)
             return sendResponse(405, "No player with : "+pseudo, "GET");
         
        int taille =  player.getCards().size();

        String [] list = new String[taille];
        for (int i = 0; i < taille; i++){
            list[i] = "{\"number\" : \""+player.getCards().get(i).getValue()+"\", " +
                       "\"familly\" : \""+player.getCards().get(i).getColor()+"\"," +
                       "\"position\" : \""+ i +"\"}";
        }
        return sendResponse(200, "{\"cartes\": "+ Arrays.toString(list)+" }", "GET");
    }
    
    /**
     * Methode piocher une carte
     * Verif user actuel est bien l'utilisateur
     */
    
    @POST
    @Path("/{gameName}/{pseudo}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response pickacard(@PathParam("gameName")String gameName,@PathParam("pseudo")String pseudo) throws JSONException {
        // Cration de tous les objets
        Model model = Model.getInstance();
        Player player = model.findPlayerByName(gameName, pseudo);
        Game game = model.findGameByName(gameName);
        
       Player verifplayer = game.getBoard().getActualPlayer();
       
       if(!player.equals(verifplayer))
           return sendResponse(405, "Joueur non autorisé à piocher", "POST");
        
        game.getBoard().drawCard();

        return sendResponse(200, "carte ajoutée à la main du joueur", "POST");
    }
    
    /**
     * Méthode en PUT permettant de jouer une carte
     * La partie doit être existante et commencée.
     * @param pseudo
     * @param gameName
     * @param strJSON {"value": int, "color": str, "actionCard": null}
     * @return Response 200 | 422 | 405
     * @throws JSONException 
     */
    @PUT 
    @Path("/{gameName}/{pseudo}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response playCard(@PathParam("pseudo") String pseudo,@PathParam("gameName") String gameName, String strJSON ) throws JSONException{
    	Model model = Model.getInstance();      
    	// Verification que la partie existe et est commencée
    	if(!model.existsGame(gameName)) {
            return sendResponse(405, "{\"error\": \"The game does not exist\"}", "PUT");
    	}
    	if(!model.findGameByName(gameName).gameBegin()) {
            return sendResponse(405, "{\"error\": \"The game does hasn't begun\"}", "PUT");
    	}
    	
    	// Verification que le joueur existe et st present dans la partie
    	Player player = model.findPlayerByName(gameName, pseudo);
    	if(player == null) {
            return sendResponse(405, "{\"error\": \"The player does not exist\"}", "PUT");
    	}
    	
    	// Verification du JSON
    	JSONObject json = new JSONObject(strJSON);
    	if(!json.has("value") || !json.has("color")) {
            return sendResponse(405, "{\"error\": \"The json object does not follow the rules\"}", "PUT");
    	}
    	
    	// Verifie que le joueur peut jouer
    	if(!model.findGameByName(gameName).getBoard().askPlayerCanPlay(player)) {
            return sendResponse(405, "{\"error\": \"The player can't play\"}", "PUT");
    	}
    	

    	// Verifie que le joueur possede la carte
    	Card card = new Card(json.getInt("value"), Color.valueOf(json.getString("color")));
    	if(!player.getCards().contains(card)) {
            return sendResponse(405, "{\"error\": \"The player does not possese this card\"}", "PUT");
    	}
    	
    	// Verifie que la carte est jouable
    	if(!model.findGameByName(gameName).getBoard().askPlayableCard(card)) {
            return sendResponse(405, "{\"error\": \"The card can't be played\"}", "PUT");
    	}
    	
    	// Finalement la carte est jouer
        model.findGameByName(gameName).getBoard().poseCard(card);

        return sendResponse(200, "{\"success\":\"The card was succesfully played\"}", "PUT");
    }
}
