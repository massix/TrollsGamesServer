package rocks.massi.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rocks.massi.data.Game;
import rocks.massi.data.GamesRepository;
import rocks.massi.data.UsersRepository;
import rocks.massi.data.joins.Ownership;
import rocks.massi.data.joins.OwnershipsRepository;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
public class ReorderService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private GamesRepository gamesRepository;

    @Autowired
    private OwnershipsRepository ownershipsRepository;

    public void reorderCollections() {
        usersRepository.findAll().forEach(user -> {
            log.info("Reordering collection for user {}", user.getEmail());

            // Get all games owned by the user
            List<Ownership> ownerships = ownershipsRepository.findByUser(user.getBggNick());
            List<Game> ownedGames = new LinkedList<>();
            ownerships.forEach(ownership -> ownedGames.add(gamesRepository.findById(ownership.getGame())));
            log.info("User {} owns {} games", user.getEmail(), ownedGames.size());

            // Sort them alphabetically
            ownedGames.sort(Comparator.comparing(Game::getName));

            // Delete all the ownerships
            ownershipsRepository.deleteByUser(user.getBggNick());
            log.info("Deleted all ownerships for {}", user.getEmail());

            // Replace all the ownerships in alphabetical order
            ownedGames.forEach(game -> ownershipsRepository.save(new Ownership(user.getBggNick(), game.getId())));
            log.info("Reordered all ownerships for {}", user.getEmail());
        });
    }
}
