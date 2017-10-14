import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Game} from './game';
import 'rxjs/add/operator/toPromise';

@Injectable()
export class GamesService {
  apiBase = "http://massi.rocks:8180/v1";
  cors = "https://cors-anywhere.herokuapp.com/";

  constructor(private httpClient : HttpClient) {}

  getGame(id: number) : Promise<Game> {
    return this.httpClient.get<Game>(this.cors + this.apiBase + "/games/get/" + id).toPromise();
  }

  getGames() : Promise<Game[]> {
    return this.httpClient.get<Game[]>(this.cors + this.apiBase + "/games/get").toPromise();
  }
}
