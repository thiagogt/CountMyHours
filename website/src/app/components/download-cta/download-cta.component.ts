import { Component } from '@angular/core';

@Component({
  selector: 'app-download-cta',
  standalone: true,
  template: `
    <section id="download" class="py-24 px-6 bg-[#1a1d27]/40">
      <div class="max-w-3xl mx-auto text-center">
        <div class="bg-[#1a1d27] border border-[#2a2d3a] rounded-2xl p-12 relative overflow-hidden">
          <!-- Glow -->
          <div class="absolute inset-0 pointer-events-none">
            <div class="absolute top-0 left-1/2 -translate-x-1/2 w-64 h-32 bg-[#6366f1]/10 blur-3xl rounded-full"></div>
          </div>

          <img src="assets/logo.png" alt="CountMyHours" class="w-20 h-20 mx-auto mb-6">
          <h2 class="text-4xl font-bold text-[#e4e4e7] mb-3">CountMyHours</h2>
          <p class="text-[#9ca3af] text-lg mb-2">Version 3.2 · macOS 12 or later</p>
          <p class="text-[#9ca3af] text-sm mb-10">Free · No subscription · Your data stays on your Mac</p>

          <div class="flex flex-col sm:flex-row gap-4 justify-center items-center">
            <a
              href="https://apps.apple.com/app/countmyhours"
              target="_blank"
              rel="noopener noreferrer"
              class="inline-flex items-center gap-3 bg-[#6366f1] hover:bg-[#4f46e5] text-white font-semibold px-8 py-4 rounded-xl text-base transition-all hover:shadow-lg hover:shadow-[#6366f1]/25 w-full sm:w-auto justify-center">
              <svg class="w-6 h-6" viewBox="0 0 24 24" fill="currentColor">
                <path d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.8-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M13 3.5c.73-.83 1.94-1.46 2.94-1.5.13 1.17-.34 2.35-1.04 3.19-.69.85-1.83 1.51-2.95 1.42-.15-1.15.41-2.35 1.05-3.11z"/>
              </svg>
              Download on the Mac App Store
            </a>
          </div>

          <div class="flex items-center justify-center gap-8 mt-10 pt-8 border-t border-[#2a2d3a]">
            <div class="flex items-center gap-2 text-[#9ca3af] text-sm">
              <svg class="w-4 h-4 text-[#6366f1]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
              </svg>
              100% Private
            </div>
            <div class="flex items-center gap-2 text-[#9ca3af] text-sm">
              <svg class="w-4 h-4 text-[#6366f1]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/>
              </svg>
              Sandboxed
            </div>
            <div class="flex items-center gap-2 text-[#9ca3af] text-sm">
              <svg class="w-4 h-4 text-[#6366f1]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 15a4 4 0 004 4h9a5 5 0 10-.1-9.999 5.002 5.002 0 10-9.78 2.096A4.001 4.001 0 003 15z"/>
              </svg>
              No Cloud
            </div>
          </div>
        </div>
      </div>
    </section>
  `
})
export class DownloadCtaComponent {}
