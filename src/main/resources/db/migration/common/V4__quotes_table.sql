create table quotes (
    author text not null,
    quote text not null,
    primary key(author, quote)
);

insert into quotes values
    ('Reiner Knizia', 'The goal is to win, but it is the goal that is important, not the winning.'),
    ('Larry Wall', 'We all agree on the necessity of compromise. We just can''t agree on when it''s necessary to compromise.'),
    ('Albert Einstein', 'You have to learn the rules of the game. And then you have to play better than anyone else.'),
    ('Shunryu Suzuki', 'In the beginner''s mind there are many possibilities, but in the expert''s mind there are few.'),
    ('Anonymous', 'Money isn''t everything, but it is a tiebreaker in Power Grid.'),
    ('Anonymous', 'The way a man plays a game shows some of his character. The way he loses shows all of it.'),
    ('Ashleigh Brilliant', 'Life is the only game in which the object of the game is to learn the rules.'),
    ('Michel de Montaigne', 'It should be noted that the games of children are not games, and must be considered as their most serious actions.'),
    ('Oliver Wendall Holmes', 'We don''t stop playing because we grow old; We grow old because we stop playing.'),
    ('Lord Salisbury', 'One of the nuisances of the ballot is that when the oracle has spoken, you never know what it means.'),
    ('Oscar Wilde', 'Of course I have played outdoor games. I once played dominoes in an open air cafe in Paris.'),
    ('C3PO', 'I suggest a new strategy, Artoo. Let the Wookiee win.'),
    ('Brian Vaughan', 'There are only three forms of high art: the symphony, the illustrated childrens'' books and the board games.');
