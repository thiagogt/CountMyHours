import { Component, signal, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-screenshots',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section id="screenshots" class="py-24 px-6 bg-[#1a1d27]/40">
      <div class="max-w-6xl mx-auto">
        <div class="text-center mb-12">
          <h2 class="text-4xl font-bold text-[#e4e4e7] mb-4">See It in Action</h2>
          <p class="text-[#9ca3af] text-lg">A clean, dark interface designed to keep you focused on the data that matters.</p>
        </div>

        <!-- Scrollable strip -->
        <div class="flex gap-4 overflow-x-auto pb-4 snap-x snap-mandatory scrollbar-hide" style="scrollbar-width: none;">
          @for (shot of screenshots; track shot.src; let i = $index) {
            <div class="flex-shrink-0 w-[600px] snap-center cursor-pointer" (click)="openLightbox(i)">
              <div class="rounded-xl overflow-hidden border border-[#2a2d3a] hover:border-[#6366f1]/50 transition-all shadow-lg hover:shadow-[#6366f1]/10">
                <div class="bg-[#1a1d27] px-3 py-2 flex items-center gap-1.5 border-b border-[#2a2d3a]">
                  <div class="w-2.5 h-2.5 rounded-full bg-[#ff5f57]"></div>
                  <div class="w-2.5 h-2.5 rounded-full bg-[#febc2e]"></div>
                  <div class="w-2.5 h-2.5 rounded-full bg-[#28c840]"></div>
                  <div class="flex-1 mx-3 text-xs text-[#9ca3af] text-center">{{ shot.label }}</div>
                </div>
                <img [src]="shot.src" [alt]="shot.label" class="w-full block">
              </div>
            </div>
          }
        </div>
        <p class="text-center text-[#9ca3af] text-sm mt-4">Click any screenshot to enlarge</p>
      </div>

      <!-- Lightbox -->
      @if (lightboxIndex() !== null) {
        <div class="fixed inset-0 z-50 bg-black/90 flex items-center justify-center p-4" (click)="closeLightbox()">
          <button class="absolute top-4 right-4 text-[#9ca3af] hover:text-white" (click)="closeLightbox()">
            <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>
          <button class="absolute left-4 top-1/2 -translate-y-1/2 text-[#9ca3af] hover:text-white" (click)="prev($event)">
            <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
            </svg>
          </button>
          <div class="max-w-5xl w-full rounded-xl overflow-hidden border border-[#2a2d3a]" (click)="$event.stopPropagation()">
            <div class="bg-[#1a1d27] px-4 py-3 flex items-center gap-2 border-b border-[#2a2d3a]">
              <div class="w-3 h-3 rounded-full bg-[#ff5f57]"></div>
              <div class="w-3 h-3 rounded-full bg-[#febc2e]"></div>
              <div class="w-3 h-3 rounded-full bg-[#28c840]"></div>
              <div class="flex-1 text-center text-sm text-[#9ca3af]">{{ screenshots[lightboxIndex()!].label }}</div>
            </div>
            <img [src]="screenshots[lightboxIndex()!].src" [alt]="screenshots[lightboxIndex()!].label" class="w-full block">
          </div>
          <button class="absolute right-4 top-1/2 -translate-y-1/2 text-[#9ca3af] hover:text-white" (click)="next($event)">
            <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
            </svg>
          </button>
        </div>
      }
    </section>
  `
})
export class ScreenshotsComponent {
  lightboxIndex = signal<number | null>(null);

  screenshots = [
    { src: 'assets/screenshots/screenshot_01.png', label: 'Dashboard' },
    { src: 'assets/screenshots/screenshot_02.png', label: 'Timeline' },
    { src: 'assets/screenshots/screenshot_03.png', label: 'Month Balance' },
    { src: 'assets/screenshots/screenshot_04.png', label: 'Extras Analysis' },
    { src: 'assets/screenshots/screenshot_05.png', label: 'Hour Selling' },
    { src: 'assets/screenshots/screenshot_06.png', label: 'Data Entry' },
    { src: 'assets/screenshots/screenshot_07.png', label: 'Import History' },
    { src: 'assets/screenshots/screenshot_08.png', label: 'Settings' },
    { src: 'assets/screenshots/screenshot_09.png', label: 'Project Timeline' },
    { src: 'assets/screenshots/screenshot_10.png', label: 'Welcome Screen' }
  ];

  openLightbox(index: number) { this.lightboxIndex.set(index); }
  closeLightbox() { this.lightboxIndex.set(null); }

  prev(e: Event) {
    e.stopPropagation();
    const i = this.lightboxIndex();
    if (i !== null) this.lightboxIndex.set((i - 1 + this.screenshots.length) % this.screenshots.length);
  }

  next(e: Event) {
    e.stopPropagation();
    const i = this.lightboxIndex();
    if (i !== null) this.lightboxIndex.set((i + 1) % this.screenshots.length);
  }

  @HostListener('document:keydown.escape')
  onEscape() { this.closeLightbox(); }

  @HostListener('document:keydown.arrowLeft')
  onLeft() { if (this.lightboxIndex() !== null) this.prev(new Event('')); }

  @HostListener('document:keydown.arrowRight')
  onRight() { if (this.lightboxIndex() !== null) this.next(new Event('')); }
}
