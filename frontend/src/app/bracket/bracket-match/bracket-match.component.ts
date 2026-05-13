import { Component, input, signal } from "@angular/core";
import { BracketSlotComponent } from "../bracket-slot/bracket-slot.component";
import { BracketMatch, MatchParticipant } from "../../models/bracket.models";
import { EventBusService } from "../../services/eventBus.service";

@Component({
    selector: "app-bracket-match",
    imports: [BracketSlotComponent],
    templateUrl: "./bracket-match.component.html",
    styleUrl: "./bracket-match.component.scss"
})
export class BracketMatchComponent {
    promoted1 = signal(false);
    promoted2 = signal(false);

    constructor(private eventBus: EventBusService) {}

    match = input.required<BracketMatch>();
    toggleSelectedParticipant(slotNumber: number, participant: MatchParticipant) {
        if (this.match().participant2 && !this.match().isComplete) {
            {
                let opponent: MatchParticipant | null;

                if (slotNumber == 1) {
                    this.promoted1.set(!this.promoted1());
                    this.promoted2.set(false);

                    opponent = this.match().participant2;
                } else {
                    this.promoted2.set(!this.promoted2());
                    this.promoted1.set(false);

                    opponent = this.match().participant1;
                }

                this.eventBus.emit("bracket-winner:select", { participant, opponent });
            }
        }
    }
}
