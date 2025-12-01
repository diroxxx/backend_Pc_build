package org.example.backend_pcbuild.Game;

import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.models.Game;
import org.example.backend_pcbuild.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;



    public Game findByTitle(String title) {
        Optional<Game> byTitle = gameRepository.findByTitle(title);
        return byTitle.orElse(null);
    }

    public List<GameDto> getAllGames(){
        List<Game> games = gameRepository.findAll();
        return games.stream().map(this::convertToDTO).toList();

    }
    private GameDto convertToDTO(Game game) {
        GameDto dto = new GameDto();

        dto.setId(game.getId());
        dto.setTitle(game.getTitle());


        if (game.getImage() != null) {
            String imageBase64 = Base64.getEncoder().encodeToString(game.getImage());
            dto.setImageBase64(imageBase64);
        }
        return dto;
    }




}
