INSERT INTO fighter (name, style, origine, image)
VALUES
('Jin Kazama', 'Karate / Devil Gene', 'Japan', 'jin.png'),
('Kazuya Mishima', 'Karate / Mishima Style', 'Japan', 'kazuya.png'),
('Heihachi Mishima', 'Karate / Mishima Style', 'Japan', 'heihachi.png'),
('King', 'Lucha Libre', 'Mexico', 'king.png'),
('Paul Phoenix', 'Judo / Brawling', 'USA', 'paul.png'),
('Nina Williams', 'Assassination / Aikido', 'Ireland', 'nina.png'),
('Hwoarang', 'Taekwondo', 'South Korea', 'hwoarang.png'),
('Ling Xiaoyu', 'Baguazhang / Tai Chi', 'China', 'xiaoyu.png'),
('Yoshimitsu', 'Ninjutsu / Cyber Ninja', 'Japan', 'yoshimitsu.png'),
('Law', 'Jeet Kune Do', 'USA', 'law.png');

INSERT INTO player (username, email, age, elo, image, id_fighter)
VALUES
('kevin', 'kevin@test.be', 25, '1200', 'jin.png', 1),
('laura', 'laura@test.be', 23, '1250', 'kazuya.png', 2),
('yassine', 'yassine@test.be', 27, '1300', 'king.png', 3),
('sofia', 'sofia@test.be', 22, '1100', 'heihachi.png', 4);

INSERT INTO tournament (status, name, start_date, end_date)
VALUES
('IN_PROGRESS', 'Tekken 8 Championship', '2026-05-10 10:00:00', NULL);

INSERT INTO registration (status, registered_date, id_player, id_tournament)
VALUES
('CONFIRMED', '2026-05-10 09:00:00', 1, 1),
('CONFIRMED', '2026-05-10 09:01:00', 2, 1),
('CONFIRMED', '2026-05-10 09:02:00', 3, 1),
('CONFIRMED', '2026-05-10 09:03:00', 4, 1);

INSERT INTO "match" (
    status,
    nb_rounds,
    bracket_position,
    scheduled_at,
    started_at,
    finished_at,
    player1_score,
    player2_score,
    id_tournament,
    id_player1,
    id_player2,
    finish_type
)
VALUES
(
    'FINISHED',
    3,
    'SEMIFINAL_1',
    '2026-05-11 18:00:00',
    '2026-05-11 18:05:00',
    '2026-05-11 18:25:00',
    2,
    1,
    1,
    1,
    2,
    'KO'
),

(
    'FINISHED',
    3,
    'SEMIFINAL_2',
    '2026-05-11 19:00:00',
    '2026-05-11 19:05:00',
    '2026-05-11 19:20:00',
    2,
    0,
    1,
    3,
    4,
    'PERFECT'
),

(
    'IN_PROGRESS',
    5,
    'FINAL',
    '2026-05-12 20:00:00',
    '2026-05-12 20:05:00',
    NULL,
    1,
    1,
    1,
    1,
    3,
    NULL
);