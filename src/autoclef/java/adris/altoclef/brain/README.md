# Brain

Brain represents the AI/LLM processing/etc logic. It is split across the following boundaries 

- Server: 
    - Global: manages all npcs/all altoclef controllers, and interactions between NPCs
    - Local: Per npc/altoclef controller logic
- Client: 
    - Global: manages this client's owned NPCs, 
    - Local:  manages specific NPC, and handles LLM/Api calls to Player2.

- Shared: utils shared by all above