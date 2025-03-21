import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { OffersComponent } from './offers/offers.component';
import { NewadComponent } from './newad/newad.component';

export const routes: Routes = [
    { path: '', component: HomeComponent },
    { path: 'offers', component: OffersComponent },
    { path: 'newad', component: NewadComponent }
];
