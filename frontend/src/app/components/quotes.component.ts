import { Component, OnInit } from '@angular/core';
import { Quote } from '../data/quote';
import { QuotesService } from '../services/quotes.service';

@Component({
    template:
    `
        <div class="container">
            <div class="table">
                <table class="table table-condensed table-hover table-striped">
                    <thead>
                        <tr>
                            <th><button class="btn btn-sm fa fa-refresh" (click)="refreshQuotes()"></button>Add/Remove</th>
                            <th role="col">Author</th>
                            <th>Quote</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr *ngFor="let quote of quotes">
                            <td>
                                <button class="btn btn-sm fa" [ngClass]="{'fa-remove': quote !== newQuote, 'fa-plus': quote === newQuote}" (click)="removeAddQuote(quote)"></button>
                                <button *ngIf="quote !== newQuote" class="btn btn-sm fa fa-save"></button>
                            </td>
                            <th role="col">
                                <inline-editor size="16" type="text" [(ngModel)]="quote.author" (onSave)="saveQuote(quote)" [disabled]="quote !== newQuote"></inline-editor>
                            </th>
                            <td>
                                <inline-editor size="70" type="text" [(ngModel)]="quote.quote" (onSave)="saveQuote(quote)" [disabled]="quote !== newQuote"></inline-editor>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div> 
    `
})
export class QuotesComponent implements OnInit {
    quotes: Quote[];
    newQuote = new Quote();

    constructor(private quotesService: QuotesService) {}

    refreshQuotes() {
        this.quotesService.getAllQuotes().subscribe(data => {
            this.quotes = data;
            this.newQuote.author = 'Insert Author';
            this.newQuote.quote = 'Insert Quote';
            this.quotes.push(this.newQuote);
        });
    }

    ngOnInit() {
        this.refreshQuotes();
    }

    saveQuote(quote: Quote) {
    }

    removeAddQuote(quote: Quote) {
        if (quote !== this.newQuote) {
            this.quotesService.removeQuote(quote).subscribe(
                data => this.refreshQuotes(),
                error => {
                    console.log('error');
                }
            )
        } else {
            this.quotesService.addQuote(this.newQuote).subscribe(
                data => this.refreshQuotes()
            )
        }
    }
}