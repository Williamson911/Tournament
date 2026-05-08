-- Allow matches to exist without players assigned (for bracket slots)
ALTER TABLE match ALTER COLUMN id_player1 DROP NOT NULL;
ALTER TABLE match ALTER COLUMN id_player2 DROP NOT NULL;
ALTER TABLE match ALTER COLUMN player1_score DROP NOT NULL;
ALTER TABLE match ALTER COLUMN player2_score DROP NOT NULL;
