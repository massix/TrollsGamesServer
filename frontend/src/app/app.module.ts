import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';

import { AppComponent } from './app.component';
import { UsersComponent } from './users.component';

import { UsersService } from './users.service';
import { GamesService } from './games.service';

@NgModule({
  declarations: [
    AppComponent,
    UsersComponent,
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    RouterModule.forRoot([
      {
        path: 'users',
        component: UsersComponent
      },
      {
        path: '',
        redirectTo: '/users',
        pathMatch: 'full'
      }
    ])
  ],
  providers: [UsersService, GamesService],
  bootstrap: [AppComponent]
})
export class AppModule { }
