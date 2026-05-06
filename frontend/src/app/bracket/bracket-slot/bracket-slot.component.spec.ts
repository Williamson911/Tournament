import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BracketSlotComponent } from './bracket-slot.component';
import { MatchParticipant } from '../../models/bracket.models';

const WINNER: MatchParticipant = {
  playerId: 1, playerName: 'MaxCombo', fighterName: 'Jin Kazama',
  fighterImageUrl: 'https://example.com/jin.jpg',
  score: 2, isWinner: true, isEliminated: false
};
const LOSER: MatchParticipant = {
  playerId: 2, playerName: 'DevilFist', fighterName: 'Kazuya',
  fighterImageUrl: '', score: 1, isWinner: false, isEliminated: false
};
const ELIMINATED: MatchParticipant = {
  playerId: 3, playerName: 'Silencer', fighterName: 'Nina',
  fighterImageUrl: '', score: 0, isWinner: false, isEliminated: true
};

describe('BracketSlotComponent', () => {
  let fixture: ComponentFixture<BracketSlotComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [BracketSlotComponent] });
    fixture = TestBed.createComponent(BracketSlotComponent);
  });

  it('affiche le nom du joueur et le score', () => {
    fixture.componentRef.setInput('participant', WINNER);
    fixture.componentRef.setInput('seed', 1);
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.slot-name')!.textContent!.trim()).toBe('MaxCombo');
    expect(el.querySelector('.slot-score')!.textContent!.trim()).toBe('2');
  });

  it('applique .winner au gagnant', () => {
    fixture.componentRef.setInput('participant', WINNER);
    fixture.componentRef.setInput('seed', 1);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.slot-wrapper').classList).toContain('winner');
  });

  it('applique .loser au perdant non éliminé', () => {
    fixture.componentRef.setInput('participant', LOSER);
    fixture.componentRef.setInput('seed', 2);
    fixture.componentRef.setInput('matchComplete', true);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.slot-wrapper').classList).toContain('loser');
  });

  it('ne dimme pas le slot si le match est en cours', () => {
    fixture.componentRef.setInput('participant', LOSER);
    fixture.componentRef.setInput('seed', 2);
    fixture.componentRef.setInput('matchComplete', false);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.slot-wrapper').classList).not.toContain('loser');
  });

  it('applique .eliminated au joueur éliminé', () => {
    fixture.componentRef.setInput('participant', ELIMINATED);
    fixture.componentRef.setInput('seed', 3);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.slot-wrapper').classList).toContain('eliminated');
  });

  it('affiche ??? quand participant est null', () => {
    fixture.componentRef.setInput('participant', null);
    fixture.componentRef.setInput('seed', '');
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.slot-name')!.textContent!.trim()).toBe('???');
  });

  it('affiche le nom du fighter sur le portrait', () => {
    fixture.componentRef.setInput('participant', WINNER);
    fixture.componentRef.setInput('seed', 1);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.portrait-label')!.textContent!.trim()).toBe('Jin Kazama');
  });
});
