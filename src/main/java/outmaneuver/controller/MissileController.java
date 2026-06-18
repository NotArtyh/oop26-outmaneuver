package outmaneuver.controller;

/**
 * Controller dei missili. È un EntityController specializzato: gestisce lo spawn,
 * il movimento e le collisioni dei missili. Il tipo dedicato serve al
 * MasterController per instradargli gli eventi di collisione dei missili.
 */
public interface MissileController extends EntityController {
}
