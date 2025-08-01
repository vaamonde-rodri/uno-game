import React, { useState } from 'react';
import { useGame } from '../hooks/useGame';
import { GameStatus } from '../types';
import './Lobby.css';

interface LobbyProps {
  onGameStart: (gameCode: string, playerId: number) => void;
}

export function Lobby({ onGameStart }: LobbyProps) {
  const [playerName, setPlayerName] = useState('');
  const [gameCodeInput, setGameCodeInput] = useState('');
  const [currentPlayerId, setCurrentPlayerId] = useState<number | null>(null);

  const {
    game,
    loading,
    error,
    connected,
    createGame,
    joinGame,
    startGame
  } = useGame(currentPlayerId || undefined);

  const handleCreateGame = async () => {
    if (!playerName.trim()) {
      alert('Por favor, ingresa tu nombre');
      return;
    }

    try {
      const createdGame = await createGame(playerName);
      // Obtener el ID del jugador de la respuesta directa
      const player = createdGame.players.find(p => p.name === playerName);
      if (player) {
        setCurrentPlayerId(player.id);
      }
    } catch (err) {
      console.error('Error creando partida:', err);
    }
  };

  const handleJoinGame = async () => {
    if (!playerName.trim()) {
      alert('Por favor, ingresa tu nombre');
      return;
    }

    if (!gameCodeInput.trim()) {
      alert('Por favor, ingresa el código de la partida');
      return;
    }

    try {
      const joinedGame = await joinGame(gameCodeInput.toUpperCase(), playerName);
      // Obtener el ID del jugador de la respuesta directa
      const player = joinedGame.players.find(p => p.name === playerName);
      if (player) {
        setCurrentPlayerId(player.id);
      }
    } catch (err) {
      console.error('Error uniéndose a la partida:', err);
    }
  };

  const handleStartGame = async () => {
    try {
      await startGame();
      if (game?.gameCode && currentPlayerId) {
        onGameStart(game.gameCode, currentPlayerId);
      }
    } catch (err) {
      console.error('Error iniciando partida:', err);
    }
  };

  const canStartGame = game &&
      game.players.length >= 2 &&
      game.status === GameStatus.WAITING_FOR_PLAYERS &&
      game.createdById === currentPlayerId;

  if (!connected) {
    return (
      <div className="lobby">
        <div className="lobby__status">
          <h2>🔌 Conectando...</h2>
          <p>Estableciendo conexión en tiempo real...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="lobby">
      <header className="lobby__header">
        <h1>🃏 UNO Online</h1>
        <p>¡Juega UNO en tiempo real con tus amigos!</p>
      </header>

      {error && (
        <div className="lobby__error">
          <p>❌ {error}</p>
        </div>
      )}

      {!game ? (
        // Pantalla inicial: crear o unirse a partida
        <div className="lobby__actions">
          <div className="lobby__section">
            <h2>👤 Tu nombre</h2>
            <input
              type="text"
              value={playerName}
              onChange={(e) => setPlayerName(e.target.value)}
              placeholder="Ingresa tu nombre"
              className="lobby__input"
              maxLength={20}
            />
          </div>

          <div className="lobby__section">
            <h2>🎮 Crear nueva partida</h2>
            <button
              onClick={handleCreateGame}
              disabled={loading || !playerName.trim()}
              className="lobby__button lobby__button--primary"
            >
              {loading ? 'Creando...' : 'Crear Partida'}
            </button>
          </div>

          <div className="lobby__section">
            <h2>🔗 Unirse a partida</h2>
            <input
              type="text"
              value={gameCodeInput}
              onChange={(e) => setGameCodeInput(e.target.value.toUpperCase())}
              placeholder="Código de partida"
              className="lobby__input"
              maxLength={6}
            />
            <button
              onClick={handleJoinGame}
              disabled={loading || !playerName.trim() || !gameCodeInput.trim()}
              className="lobby__button lobby__button--secondary"
            >
              {loading ? 'Uniéndose...' : 'Unirse'}
            </button>
          </div>
        </div>
      ) : (
        // Pantalla de espera: mostrar jugadores y esperar a que inicie la partida
        <div className="lobby__waiting">
          <div className="lobby__game-info">
            <h2>🎲 Partida: {game.gameCode}</h2>
            <p className="lobby__game-code">
              Comparte este código con tus amigos: <strong>{game.gameCode}</strong>
            </p>
          </div>

          <div className="lobby__players">
            <h3>👥 Jugadores ({game.players.length}/4)</h3>
            <div className="lobby__players-list">
              {game.players.map((player) => (
                <div
                  key={player.id}
                  className={`lobby__player ${player.id === currentPlayerId ? 'lobby__player--self' : ''}`}
                >
                  <span className="lobby__player-name">{player.name}</span>
                  {player.id === currentPlayerId && <span className="lobby__player-badge">Tú</span>}
                  {player.id === game.createdById && <span className="lobby__player-badge lobby__player-badge--host">👑 Host</span>}
                </div>
              ))}
            </div>
          </div>

          <div className="lobby__game-controls">
            {game.status === GameStatus.WAITING_FOR_PLAYERS ? (
              <>
                <p className="lobby__status-text">
                  {game.players.length < 2
                    ? 'Esperando más jugadores...'
                    : '¡Listos para jugar!'}
                </p>
                {canStartGame && (
                  <button
                    onClick={handleStartGame}
                    disabled={loading}
                    className="lobby__button lobby__button--start"
                  >
                    {loading ? 'Iniciando...' : '🚀 Iniciar Partida'}
                  </button>
                )}
              </>
            ) : game.status === GameStatus.IN_PROGRESS ? (
              <div className="lobby__status-text">
                ¡La partida ha comenzado!
              </div>
            ) : (
              <div className="lobby__status-text">
                La partida ha terminado
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
