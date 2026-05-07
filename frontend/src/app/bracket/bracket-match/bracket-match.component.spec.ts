import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BracketMatchComponent } from './bracket-match.component';
import { BracketMatch } from '../../models/bracket.models';

const MATCH: BracketMatch = {
  matchId: 1,
  participant1: { playerId: 1, playerName: 'MaxCombo', fighterName: 'Jin Kazama', fighterImageUrl: '', score: 2, isWinner: true, isEliminated: false },
  participant2: { playerId: 2, playerName: 'DevilFist', fighterName: 'Kazuya', fighterImageUrl: '', score: 1, isWinner: false, isEliminated: false },
  isComplete: true
};

const TBD: BracketMatch = {
  matchId: 2, participant1: null, participant2: null,
  isComplete: false, isBracketReset: false
};

describe('BracketMatchComponent', () => {
  let fixture: ComponentFixture<BracketMatchComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [BracketMatchComponent] });
    fixture = TestBed.createComponent(BracketMatchComponent);
  });

  it('rend deux app-bracket-slot', () => {
    fixture.componentRef.setInput('match', MATCH);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('app-bracket-slot').length).toBe(2);
  });

  it('rend la barre VS', () => {
    fixture.componentRef.setInput('match', MATCH);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.vs-divider')).toBeTruthy();
  });

  it('fonctionne avec participants null (TBD)', () => {
    fixture.componentRef.setInput('match', TBD);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('app-bracket-slot').length).toBe(2);
  });
});
