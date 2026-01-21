package org.project.backend_pcbuild.unitTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.backend_pcbuild.Game.dto.GameDto;
import org.project.backend_pcbuild.Game.model.Game;
import org.project.backend_pcbuild.Game.repository.GameRepository;
import org.project.backend_pcbuild.Game.service.GameService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GameServiceTest {


    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameService gameService;




    @Test
    void getAllGamesTest(){
        Game testGame;
        testGame = new Game();
        testGame.setTitle("testGame");
        testGame.setId(1L);

        when(gameRepository.findAll())
                .thenReturn(List.of(testGame));

        List<GameDto> gameDtoList = gameService.getAllGames();

        assertNotNull(gameDtoList );
        assertEquals( 1, gameDtoList.size());
    }

    @Test
    void findByTitleGameTest(){
        Game expectedGame = new Game();
        expectedGame.setTitle("testGame");
        expectedGame.setId(1L);

        when(gameRepository.findByTitle("testGame"))
                .thenReturn(Optional.of(expectedGame));

        Game actualGame = gameService.findByTitle(expectedGame.getTitle());

        assertNotNull(actualGame);
        assertEquals(expectedGame.getId(), actualGame.getId());
        assertEquals(expectedGame.getTitle(), actualGame.getTitle());

    }


}
