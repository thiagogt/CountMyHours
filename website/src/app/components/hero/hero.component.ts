import { Component } from '@angular/core';

@Component({
  selector: 'app-hero',
  standalone: true,
  template: `
    <section class="min-h-screen flex items-center pt-20 pb-16 px-6 relative overflow-hidden">
      <!-- Background glow -->
      <div class="absolute inset-0 pointer-events-none">
        <div class="absolute top-1/4 left-1/4 w-96 h-96 bg-[#6366f1]/10 rounded-full blur-3xl"></div>
        <div class="absolute bottom-1/4 right-1/4 w-64 h-64 bg-[#6366f1]/5 rounded-full blur-3xl"></div>
      </div>

      <div class="max-w-6xl mx-auto w-full relative z-10">
        <div class="grid lg:grid-cols-2 gap-16 items-center">
          <!-- Left: text + CTAs -->
          <div>
            <div class="inline-flex items-center gap-2 bg-[#6366f1]/10 border border-[#6366f1]/20 rounded-full px-4 py-1.5 mb-6">
              <span class="w-2 h-2 bg-[#6366f1] rounded-full"></span>
              <span class="text-[#6366f1] text-sm font-medium">Available for macOS · Windows Coming Soon</span>
            </div>
            <h1 class="text-5xl lg:text-6xl font-bold text-[#e4e4e7] leading-tight mb-6">
              Track Every Hour.<br>
              <span class="text-[#6366f1]">Own Your Time.</span>
            </h1>
            <p class="text-[#9ca3af] text-xl leading-relaxed mb-10">
              Import your timesheets, visualize project hours, and analyze overtime —
              all stored locally on your device. No cloud. No subscription. Pay once, keep forever.
            </p>
            <div class="flex flex-wrap gap-4">
              <a href="#download" class="inline-flex items-center gap-2 bg-[#6366f1] hover:bg-[#4f46e5] text-white font-semibold px-8 py-4 rounded-xl text-base transition-all hover:shadow-lg hover:shadow-[#6366f1]/25">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"/>
                </svg>
                Get CountMyHours
              </a>
              <a href="#screenshots" class="inline-flex items-center gap-2 border border-[#2a2d3a] hover:border-[#6366f1]/50 text-[#e4e4e7] font-medium px-8 py-4 rounded-xl text-base transition-all">
                See Screenshots
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
                </svg>
              </a>
            </div>
            <div class="flex items-center gap-8 mt-10">
              <div class="text-center">
                <div class="text-2xl font-bold text-[#e4e4e7]">9</div>
                <div class="text-[#9ca3af] text-xs">Languages</div>
              </div>
              <div class="w-px h-10 bg-[#2a2d3a]"></div>
              <div class="text-center">
                <div class="text-2xl font-bold text-[#e4e4e7]">100%</div>
                <div class="text-[#9ca3af] text-xs">Private</div>
              </div>
              <div class="w-px h-10 bg-[#2a2d3a]"></div>
              <div class="text-center">
                <div class="text-2xl font-bold text-[#e4e4e7]">$9.99</div>
                <div class="text-[#9ca3af] text-xs">One-time</div>
              </div>
            </div>
          </div>

          <!-- Right: screenshot in macOS window chrome -->
          <div class="relative">
            <div class="relative rounded-xl overflow-hidden shadow-2xl shadow-black/60 border border-[#2a2d3a]">
              <!-- Window chrome -->
              <div class="bg-[#1a1d27] px-4 py-3 flex items-center gap-2 border-b border-[#2a2d3a]">
                <div class="w-3 h-3 rounded-full bg-[#ff5f57]"></div>
                <div class="w-3 h-3 rounded-full bg-[#febc2e]"></div>
                <div class="w-3 h-3 rounded-full bg-[#28c840]"></div>
                <div class="flex-1 mx-4 bg-[#0f1117] rounded text-center text-xs text-[#9ca3af] py-0.5">
                  CountMyHours
                </div>
              </div>
              <img src="assets/screenshots/screenshot_01.png" alt="CountMyHours Dashboard" class="w-full block">
            </div>
            <!-- Floating badge -->
            <div class="absolute -bottom-4 -right-4 bg-[#6366f1] text-white rounded-xl px-4 py-2 text-sm font-medium shadow-lg">
              macOS · Windows
            </div>
          </div>
        </div>
      </div>
    </section>
  `
})
export class HeroComponent {}
