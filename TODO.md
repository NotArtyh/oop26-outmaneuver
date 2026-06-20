- ! fix shield virtualThread possible race condition
  - reuse SpeedBoost solution if right

- ! fix GameSession.score data race

- separate frame timing and fsm logic from masterController
  - proper game loop system with thread safety on long running sessions

- missing collectible json
- decide weather to keep bank state for sprite "animation"

- abstract game view from entities to be separate

- missile interface definition
- split missile implementation form abstract class
- utilize renderState for rendering and collectibles
- use entityController for missiles and collectibles

sistemare hud che non richieda il plane --> ho bisogno che spinac mi gestisca l'internal event in modo tale da triggerarmi l'evento di passare lo stato di shield e di velocità nell'hud senza avere metodi del tipo plane.getEffectiveSpeed(),
plane.isShieldActive(),(in HudController.java)

No, il design attuale è corretto così com'è. Ci sono due ragioni:
Velocità e scudo sono stato volatile che cambia a ogni frame — non eventi discreti. getEffectiveSpeed() ha un timer interno (multiplierEndTime) che scade in PlaneImpl senza pubblicare eventi. isShieldActive() viene settato da ShieldPowerUp su un virtual thread. Dovresti aggiungere 4 nuovi InternalEvent (SHIELD_ON, SHIELD_OFF, SPEED_BOOST, SPEED_EXPIRED) solo per replicare quello che una lettura diretta già fa correttamente.

inoltre ho bisogno che spinac mi passi un internal event anche per la collissione missile-missile, ne ho bisogno per calcolare il punteggio che è dato da: tempo di vita, star collezionate e missili fatti scontrare. (per ora lo fa con l'internalevent listener delle star collected)

inoltre ragionare sul fatto se lasciare le chiamate di internaleventlistener per le collisioni dentro mastercontroller (ultime righe) oppure portarle all'interno di collision engine.

capire perche tutti i controller estendono internalEventListener mentre CollisionEngine lo inizializza al suo interno e mastercontrollerimpl lo implementa


piano di sitemazione per lo spawn delle entities:

Piano di semplificazione
Analisi attuale
- SwingGameView usa cameraX = planeData.getX(), cameraY = planeData.getY() come centro camera
- Conversione mondo→schermo: world - camera + screenCenter
- Quindi camera center in world coords = plane position
- Il codice attuale cerca il Plane per ottenere cx, cy = camera center
Soluzione proposta (Opzione A - Minima)
1. Aggiungi a GameView interface:
Vector2 getCameraCenter(); // coordinate mondo del centro schermo
2. Implementa in SwingGameView:
@Override
public Vector2 getCameraCenter() {
    var state = latestState;
    if (state == null) return null;
    var plane = state.getPlane();
    return new Vector2(plane.getX(), plane.getY());
}
3. Semplifica CollectibleControllerImpl.randomEdgePosition():
private Vector2 randomEdgePosition() {
    if (getView() == null) return null;
    var cameraCenter = getView().getCameraCenter();
    if (cameraCenter == null) return null;
    
    final int w = getView().getWidth();
    final int h = getView().getHeight();
    if (w <= 0 || h <= 0) return null;
    
    final double cx = cameraCenter.getX();
    final double cy = cameraCenter.getY();
    final double hw = w / 2.0;
    final double hh = h / 2.0;
    
    return switch (random.nextInt(4)) {
        case 0 -> new Vector2(cx + (random.nextDouble() * 2 - 1) * hw, cy - hh); // top
        case 1 -> new Vector2(cx + (random.nextDouble() * 2 - 1) * hw, cy + hh); // bottom
        case 2 -> new Vector2(cx - hw, cy + (random.nextDouble() * 2 - 1) * hh); // left
        default -> new Vector2(cx + hw, cy + (random.nextDouble() * 2 - 1) * hh); // right
    };
}
Risultato: Rimossi 7 righe di stream/filter/map/findFirst + dipendenza da entity Plane. Stesso comportamento esatto.



- AGGIUSTARE PROBLEMA CHE QUANDO PREMO PLAY AGAIN MI GIRA DA SOLO L'AEREO