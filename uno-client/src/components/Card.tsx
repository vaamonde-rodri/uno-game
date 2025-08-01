import React from 'react';
import { CardDTO, Color, CardValue } from '../types';
import './Card.css';

interface CardProps {
  card: CardDTO;
  onClick?: () => void;
  isPlayable?: boolean;
  size?: 'small' | 'medium' | 'large';
}

export function Card({ card, onClick, isPlayable = true, size = 'medium' }: CardProps) {
  const getCardColorClass = (color: Color): string => {
    switch (color) {
      case Color.RED:
        return 'card--red';
      case Color.YELLOW:
        return 'card--yellow';
      case Color.GREEN:
        return 'card--green';
      case Color.BLUE:
        return 'card--blue';
      case Color.WILD:
        return 'card--wild';
      default:
        return 'card--wild';
    }
  };

  const getCardSymbol = (value: CardValue): string => {
    switch (value) {
      case CardValue.ZERO: return '0';
      case CardValue.ONE: return '1';
      case CardValue.TWO: return '2';
      case CardValue.THREE: return '3';
      case CardValue.FOUR: return '4';
      case CardValue.FIVE: return '5';
      case CardValue.SIX: return '6';
      case CardValue.SEVEN: return '7';
      case CardValue.EIGHT: return '8';
      case CardValue.NINE: return '9';
      case CardValue.SKIP: return '🚫';
      case CardValue.REVERSE: return '↔️';
      case CardValue.DRAW_TWO: return '+2';
      case CardValue.WILD: return '🌈';
      case CardValue.WILD_DRAW_FOUR: return '+4';
      default: return '?';
    }
  };

  const isWildCard = card.value === CardValue.WILD || card.value === CardValue.WILD_DRAW_FOUR;

  const cardClasses = [
    'card',
    `card--${size}`,
    getCardColorClass(card.color),
    isPlayable ? 'card--playable' : 'card--disabled',
    isWildCard ? 'card--wild-pattern' : ''
  ].filter(Boolean).join(' ');

  return (
    <div
      className={cardClasses}
      onClick={isPlayable ? onClick : undefined}
      role={onClick ? 'button' : 'img'}
      tabIndex={onClick && isPlayable ? 0 : -1}
      onKeyDown={(e) => {
        if ((e.key === 'Enter' || e.key === ' ') && onClick && isPlayable) {
          e.preventDefault();
          onClick();
        }
      }}
      aria-label={`Carta ${card.color} ${card.value}`}
    >
      <div className="card__content">
        <div className="card__symbol">
          {getCardSymbol(card.value)}
        </div>
        <div className="card__corner card__corner--top">
          {getCardSymbol(card.value)}
        </div>
        <div className="card__corner card__corner--bottom">
          {getCardSymbol(card.value)}
        </div>
      </div>
    </div>
  );
}
