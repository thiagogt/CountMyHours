import { Component } from '@angular/core';
import { NavbarComponent } from './components/navbar/navbar.component';
import { HeroComponent } from './components/hero/hero.component';
import { FeaturesComponent } from './components/features/features.component';
import { ScreenshotsComponent } from './components/screenshots/screenshots.component';
import { HowItWorksComponent } from './components/how-it-works/how-it-works.component';
import { LanguagesComponent } from './components/languages/languages.component';
import { YoutubeComponent } from './components/youtube/youtube.component';
import { DownloadCtaComponent } from './components/download-cta/download-cta.component';
import { SupportComponent } from './components/support/support.component';
import { FooterComponent } from './components/footer/footer.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    NavbarComponent,
    HeroComponent,
    FeaturesComponent,
    ScreenshotsComponent,
    HowItWorksComponent,
    LanguagesComponent,
    YoutubeComponent,
    DownloadCtaComponent,
    SupportComponent,
    FooterComponent
  ],
  template: `
    <app-navbar />
    <main>
      <app-hero />
      <app-features />
      <app-screenshots />
      <app-how-it-works />
      <app-languages />
      <app-youtube />
      <app-download-cta />
      <app-support />
    </main>
    <app-footer />
  `
})
export class AppComponent {}
