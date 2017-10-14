import {Component, Input} from '@angular/core';
import {Game} from './game';

@Component({
  selector: 'game-details',
  templateUrl: './game-details.component.html'
})
export class GameDetailsComponent {
  @Input() game: Game;
}
