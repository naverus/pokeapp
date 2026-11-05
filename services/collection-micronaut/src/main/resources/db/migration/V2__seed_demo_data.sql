-- Demo credentials: admin / admin123 (ADMIN), ash / pikachu123 (USER)
INSERT INTO users (username, password_hash, role)
VALUES ('admin', '$2a$10$VmzP8J9Lw35jDmiuRKI3euYgEMx3KMVhpYncg4l.8YZNrdlh3O.YW', 'ADMIN'),
       ('ash', '$2a$10$iUgg7ye7Fd9szCBRvwdaq.gWiW99XZkMOM4J92aRkm9RQ5doatYFW', 'USER');

INSERT INTO pokemon (id, pokeapi_id, name, local_name, region, category, height_m, weight_kg, sprite_url, description)
VALUES (1, 1, 'bulbasaur', 'Bulbasaur', 'Kanto', 'Seed Pokémon', 0.7, 6.9,
        'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png',
        'A strange seed was planted on its back at birth. The plant sprouts and grows with this Pokémon.'),
       (2, 4, 'charmander', 'Charmander', 'Kanto', 'Lizard Pokémon', 0.6, 8.5,
        'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/4.png',
        'Obviously prefers hot places. When it rains, steam is said to spout from the tip of its tail.'),
       (3, 7, 'squirtle', 'Squirtle', 'Kanto', 'Tiny Turtle Pokémon', 0.5, 9.0,
        'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/7.png',
        'After birth, its back swells and hardens into a shell. Powerfully sprays foam from its mouth.'),
       (4, 25, 'pikachu', 'Pikachu', 'Kanto', 'Mouse Pokémon', 0.4, 6.0,
        'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png',
        'When several of these Pokémon gather, their electricity could build and cause lightning storms.'),
       (5, 6, 'charizard', 'Charizard', 'Kanto', 'Flame Pokémon', 1.7, 90.5,
        'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/6.png',
        'Spits fire that is hot enough to melt boulders. Known to cause forest fires unintentionally.'),
       (6, 133, 'eevee', 'Eevee', 'Kanto', 'Evolution Pokémon', 0.3, 6.5,
        'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/133.png',
        'Its genetic code is irregular. It may mutate if it is exposed to radiation from element stones.'),
       (7, 143, 'snorlax', 'Snorlax', 'Kanto', 'Sleeping Pokémon', 2.1, 460.0,
        'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/143.png',
        'Very lazy. Just eats and sleeps. As its rotund bulk builds, it becomes steadily more slothful.'),
       (8, 150, 'mewtwo', 'Mewtwo', 'Kanto', 'Genetic Pokémon', 2.0, 122.0,
        'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/150.png',
        'It was created by a scientist after years of horrific gene splicing and DNA engineering experiments.'),
       (9, 94, 'gengar', 'Gengar', 'Kanto', 'Shadow Pokémon', 1.5, 40.5,
        'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/94.png',
        'Under a full moon, this Pokémon likes to mimic the shadows of people and laugh at their fright.'),
       (10, 149, 'dragonite', 'Dragonite', 'Kanto', 'Dragon Pokémon', 2.2, 210.0,
        'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/149.png',
        'An extremely rarely seen marine Pokémon. Its intelligence is said to match that of humans.');

INSERT INTO pokemon_type (pokemon_id, position, type_name)
VALUES (1, 0, 'grass'), (1, 1, 'poison'),
       (2, 0, 'fire'),
       (3, 0, 'water'),
       (4, 0, 'electric'),
       (5, 0, 'fire'), (5, 1, 'flying'),
       (6, 0, 'normal'),
       (7, 0, 'normal'),
       (8, 0, 'psychic'),
       (9, 0, 'ghost'), (9, 1, 'poison'),
       (10, 0, 'dragon'), (10, 1, 'flying');

INSERT INTO pokemon_ability (pokemon_id, position, ability_name)
VALUES (1, 0, 'overgrow'), (1, 1, 'chlorophyll'),
       (2, 0, 'blaze'), (2, 1, 'solar-power'),
       (3, 0, 'torrent'), (3, 1, 'rain-dish'),
       (4, 0, 'static'), (4, 1, 'lightning-rod'),
       (5, 0, 'blaze'), (5, 1, 'solar-power'),
       (6, 0, 'run-away'), (6, 1, 'adaptability'), (6, 2, 'anticipation'),
       (7, 0, 'immunity'), (7, 1, 'thick-fat'), (7, 2, 'gluttony'),
       (8, 0, 'pressure'), (8, 1, 'unnerve'),
       (9, 0, 'cursed-body'),
       (10, 0, 'inner-focus'), (10, 1, 'multiscale');

INSERT INTO pokemon_tag (pokemon_id, position, tag)
VALUES (1, 0, 'starter'), (1, 1, 'grass-team'),
       (2, 0, 'starter'), (2, 1, 'fire-team'),
       (3, 0, 'starter'), (3, 1, 'water-team'),
       (4, 0, 'mascot'), (4, 1, 'fan-favorite'),
       (5, 0, 'fan-favorite'),
       (6, 0, 'multi-evolution'),
       (7, 0, 'heavyweight'),
       (8, 0, 'legendary'),
       (9, 0, 'ghost-team'),
       (10, 0, 'pseudo-legendary');
