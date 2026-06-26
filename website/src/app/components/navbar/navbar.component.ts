import { Component, HostListener, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule],
  template: `
    <nav [class]="'fixed top-0 left-0 right-0 z-50 transition-all duration-300 ' + (scrolled() ? 'bg-[#0f1117]/95 backdrop-blur-md border-b border-[#2a2d3a] shadow-lg' : 'bg-transparent')">
      <div class="max-w-6xl mx-auto px-6 py-4 flex items-center justify-between">
        <a href="#" class="flex items-center gap-3">
          <img src="assets/logo.png" alt="CountMyHours" class="w-9 h-9">
          <span class="text-[#e4e4e7] font-semibold text-lg tracking-tight">CountMyHours</span>
        </a>
        <div class="hidden md:flex items-center gap-8">
          <a href="#features" class="text-[#9ca3af] hover:text-[#e4e4e7] text-sm transition-colors">Features</a>
          <a href="#screenshots" class="text-[#9ca3af] hover:text-[#e4e4e7] text-sm transition-colors">Screenshots</a>
          <a href="#tutorial" class="text-[#9ca3af] hover:text-[#e4e4e7] text-sm transition-colors">Tutorial</a>
          <a href="#download" class="bg-[#6366f1] hover:bg-[#4f46e5] text-white text-sm px-4 py-2 rounded-lg font-medium transition-colors">
            Download
          </a>
        </div>
        <button class="md:hidden text-[#9ca3af]" (click)="toggleMobile()">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              [attr.d]="mobileOpen() ? 'M6 18L18 6M6 6l12 12' : 'M4 6h16M4 12h16M4 18h16'"/>
          </svg>
        </button>
      </div>
      @if (mobileOpen()) {
        <div class="md:hidden bg-[#0f1117]/98 border-t border-[#2a2d3a] px-6 py-4 flex flex-col gap-4">
          <a href="#features" class="text-[#9ca3af] hover:text-[#e4e4e7] text-sm" (click)="mobileOpen.set(false)">Features</a>
          <a href="#screenshots" class="text-[#9ca3af] hover:text-[#e4e4e7] text-sm" (click)="mobileOpen.set(false)">Screenshots</a>
          <a href="#tutorial" class="text-[#9ca3af] hover:text-[#e4e4e7] text-sm" (click)="mobileOpen.set(false)">Tutorial</a>
          <a href="#download" class="bg-[#6366f1] text-white text-sm px-4 py-2 rounded-lg font-medium text-center" (click)="mobileOpen.set(false)">Download</a>
        </div>
      }
    </nav>
  `
})
export class NavbarComponent {
  scrolled = signal(false);
  mobileOpen = signal(false);

  @HostListener('window:scroll')
  onScroll() {
    this.scrolled.set(window.scrollY > 20);
  }

  toggleMobile() {
    this.mobileOpen.update(v => !v);
  }
}
