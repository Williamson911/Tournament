serve?
# Tournament Manager — Présentation

> Une application web pour organiser des tournois de Tekken, de A à Z.

---

## Partie 1 — Le projet

### 1.1 Le problème qu'on résout

Quand on organise un tournoi de Tekken (ou n'importe quel jeu de combat), il faut gérer plein de choses :

- **Inscrire les joueurs** (qui joue ? avec quel personnage ?)
- **Faire des poules** pour éliminer les moins forts au début
- **Dessiner le tableau du tournoi** (le « bracket ») qui montre qui affronte qui
- **Suivre les résultats** match après match
- **Désigner un vainqueur** à la fin

Aujourd'hui, la plupart des organisateurs font ça **à la main**, sur papier ou sur Excel. Résultat :

- C'est **lent** (une heure pour faire le tableau de 32 joueurs)
- C'est **plein d'erreurs** (oublis, mauvais noms, joueurs en double)
- C'est **moche** : impossible de montrer le bracket aux spectateurs en direct
- Et **personne ne comprend rien** quand il y a 32 joueurs et un système à double élimination (le perdant a une seconde chance)

### 1.2 Ce qu'on propose

Une **application web** où l'organisateur clique sur des boutons et tout se fait tout seul :

1. **32 joueurs sont déjà inscrits** au lancement (pour la démo).
2. Clic sur **« Lancer les poules »** → l'application tire au sort 16 qualifiés.
3. Clic sur **« Générer le bracket »** → le tableau du tournoi apparaît automatiquement à l'écran.
4. Clic sur **« Commencer le tournoi »** → les matchs se jouent tout seuls (simulation), on voit les gagnants avancer en direct.
5. À la fin, **le champion est affiché** sur une grande carte.
6. Bouton **« Reset »** pour tout recommencer.

### 1.3 À qui c'est destiné

- Les **organisateurs de tournois Tekken** (salles d'arcade, écoles, événements amateurs)
- Les **streamers** qui veulent montrer le bracket en direct à leurs spectateurs
- Toute personne qui en a marre de gérer ses tournois sur Excel

**En résumé** : on remplace une heure de boulot par 4 clics.

---

## Partie 2 — Le code

L'application a deux parties qui parlent entre elles :

- **Le back-end** (en Java) : c'est le « cerveau ». Il gère les joueurs, les matchs, les règles du tournoi, et il garde tout en mémoire dans une base de données.
- **Le front-end** (en Angular/TypeScript) : c'est ce que l'utilisateur voit dans son navigateur. Il affiche les boutons et le bracket.

### 2.1 Code back-end — le service qui simule un round

Quand on clique sur **« Combat suivant »**, le back-end doit :
1. Trouver tous les matchs où les deux joueurs sont prêts.
2. Tirer au sort un score pour chacun (ex : 2-1, 3-0…).
3. Désigner un gagnant et un perdant.
4. **Placer le gagnant dans le match suivant** (côté gagnants).
5. **Placer le perdant dans le tableau des perdants** (parce qu'on est en double élimination, il a encore sa chance).

Voici la version simplifiée du code :

```java
public class TournamentSimulationService {

    public void simulateNextRound(int tournamentId) {

        // 1. On récupère tous les matchs prêts à être joués
        List<Match> matchsPrets = chercherMatchsPrets(tournamentId);

        // 2. Si plus aucun match n'est prêt, le tournoi est fini
        if (matchsPrets.isEmpty()) {
            tournoi.setStatus(FINISHED);
            return;
        }

        // 3. Pour chaque match, on simule un résultat
        for (Match match : matchsPrets) {
            int[] scores = tirerScoresAuHasard();      // ex: [3, 1]
            Player gagnant = scores[0] > scores[1] ? match.getPlayer1() : match.getPlayer2();
            Player perdant = scores[0] > scores[1] ? match.getPlayer2() : match.getPlayer1();

            match.setPlayer1Score(scores[0]);
            match.setPlayer2Score(scores[1]);
            match.setStatus(FINISHED);

            // 4. On envoie le gagnant et le perdant dans les bons matchs suivants
            placerJoueur(gagnant, match.getProchainMatchGagnant());
            placerJoueur(perdant, match.getProchainMatchPerdant());
        }
    }
}
```

**Ce qu'il faut retenir** :
- Le back-end fait **tout le travail compliqué** (tirage au sort, calcul du gagnant, placement dans le bracket).
- Le front-end n'a **rien à savoir** des règles du tournoi : il dit juste « simule un round », et le back-end répond.

### 2.2 Code front-end — la page du bracket

Côté navigateur, on affiche des boutons différents **selon l'état du tournoi** :

- Si le tournoi est en `DRAFT` (juste créé) → bouton **« Lancer les poules »**
- Si le tournoi est en `GROUP_STAGE_COMPLETE` (poules finies) → bouton **« Générer le bracket »**
- Si le tournoi est `IN_PROGRESS` (en cours) → bouton **« Combat suivant »**

Voici le code Angular qui gère ça :

```typescript
export class BracketPageComponent {

  // Les données du tournoi (mises à jour automatiquement à chaque clic)
  data = signal<TournamentBracketData | null>(null);

  // Quand on clique sur "Lancer les poules"
  launchGroupStage() {
    this.service.launchGroupStage(this.tournamentId).subscribe(() => {
      this.loadBracket();   // on recharge l'affichage
    });
  }

  // Quand on clique sur "Générer le bracket"
  generateBracket() {
    this.service.generateBracket(this.tournamentId).subscribe(() => {
      this.loadBracket();
    });
  }

  // Quand on clique sur "Combat suivant"
  simulateRound() {
    this.service.simulateNextRound(this.tournamentId).subscribe(nouveauBracket => {
      this.data.set(nouveauBracket);   // on met à jour l'écran
    });
  }
}
```

Et dans le HTML, les boutons s'affichent **automatiquement** selon le statut :

```html
@if (data()?.status === 'DRAFT') {
  <button (click)="launchGroupStage()">Lancer les poules</button>
}
@if (data()?.status === 'GROUP_STAGE_COMPLETE') {
  <button (click)="generateBracket()">Générer le bracket</button>
}
@if (data()?.status === 'IN_PROGRESS') {
  <button (click)="simulateRound()">Combat suivant</button>
}
```

**Ce qu'il faut retenir** :
- Le front-end ne fait que **demander des choses au back-end** et **afficher la réponse**.
- L'utilisateur ne voit jamais la complexité : il clique sur un bouton, le bracket se met à jour tout seul.

---

## Conclusion

Tournament Manager, c'est :

- **Un produit simple** pour résoudre un vrai problème : organiser un tournoi de Tekken sans se prendre la tête.
- **Une appli web moderne** : un back-end Java solide qui gère les règles, un front-end Angular fluide qui affiche le tout.
- **Un flow ultra-court** : 4 clics pour passer de 32 inscrits à un champion.

**Démo en 1 minute** : on lance les poules → on génère le bracket → on lance la simulation → on voit le champion.
