import React, { useState } from 'react';
import { Lobby } from './pages/Lobby';
import './App.css';

type AppState = 'lobby' | 'game';

function App() {
  const [currentState, setCurrentState] = useState<AppState>('lobby');
  const [gameCode, setGameCode] = useState<string>('');
  const [playerId, setPlayerId] = useState<number>(0);

  const handleGameStart = (gameCode: string, playerId: number) => {
    setGameCode(gameCode);
    setPlayerId(playerId);
    setCurrentState('game');
  };

  const handleBackToLobby = () => {
    setCurrentState('lobby');
    setGameCode('');
    setPlayerId(0);
  };

  return (
    <div className="app">
      {currentState === 'lobby' ? (
        <Lobby onGameStart={handleGameStart} />
      ) : (
        <div className="game-placeholder">
          <h2>🎮 ¡Partida iniciada!</h2>
          <p>Código: {gameCode}</p>
          <p>Jugador ID: {playerId}</p>
          <button onClick={handleBackToLobby}>
            Volver al Lobby
          </button>
          <p><em>El tablero de juego se implementará próximamente...</em></p>
        </div>
      )}
    </div>
  );
}

export default App;
