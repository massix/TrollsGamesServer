package rocks.massi.data;

import org.springframework.data.repository.Repository;

import java.util.List;

@org.springframework.stereotype.Repository
public interface HonorsRepository extends Repository<Honor, Integer> {
    Honor save(final Honor honor);

    void deleteAll();

    void deleteById(final int id);

    Honor findById(final int id);

    List<Honor> findAll();
}
