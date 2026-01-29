"""
LLM Explainer - Natural Language Explanations for Railway Decisions
Uses Groq API (Llama 3) with Gemini fallback for speed and reliability.
"""

import os
import json
from typing import Dict, List, Optional


class LLMExplainer:
    """
    Generates natural language explanations for railway decisions.
    Uses Groq (fast) with Gemini fallback (reliable).
    """
    
    def __init__(self, groq_api_key: Optional[str] = None, gemini_api_key: Optional[str] = None):
        """
        Initialize LLM explainer with API keys.
        
        Args:
            groq_api_key: Groq API key (optional, can use env var)
            gemini_api_key: Gemini API key (optional, can use env var)
        """
        self.groq_api_key = groq_api_key or os.getenv('GROQ_API_KEY')
        self.gemini_api_key = gemini_api_key or os.getenv('GEMINI_API_KEY')
        
        self.groq_available = False
        self.gemini_available = False
        
        # Try to import and initialize Groq
        try:
            from groq import Groq
            if self.groq_api_key:
                self.groq_client = Groq(api_key=self.groq_api_key)
                self.groq_available = True
                print("✓ Groq API initialized (primary)")
        except Exception as e:
            print(f"⚠️  Groq not available: {e}")
        
        # Try to import and initialize Gemini
        try:
            import google.generativeai as genai
            if self.gemini_api_key:
                genai.configure(api_key=self.gemini_api_key)
                self.gemini_model = genai.GenerativeModel('gemini-pro')
                self.gemini_available = True
                print("✓ Gemini API initialized (fallback)")
        except Exception as e:
            print(f"⚠️  Gemini not available: {e}")
        
        if not self.groq_available and not self.gemini_available:
            print("⚠️  No LLM APIs available. Using fallback explanations.")
    
    def explain_decision(self, solution: Dict, conflict: Dict, 
                        train_a_info: Dict, train_b_info: Dict,
                        alternative_solutions: Optional[List[Dict]] = None) -> str:
        """
        Generate natural language explanation for a decision.
        
        Args:
            solution: The chosen solution dict
            conflict: The conflict dict
            train_a_info: Info about train A
            train_b_info: Info about train B
            alternative_solutions: Other solutions considered (optional)
            
        Returns:
            Natural language explanation string
        """
        
        # Build context for LLM
        context = self._build_context(solution, conflict, train_a_info, train_b_info, alternative_solutions)
        
        # Try Groq first (faster)
        if self.groq_available:
            try:
                explanation = self._call_groq(context)
                return explanation
            except Exception as e:
                print(f"⚠️  Groq failed: {e}. Trying Gemini...")
        
        # Fallback to Gemini
        if self.gemini_available:
            try:
                explanation = self._call_gemini(context)
                return explanation
            except Exception as e:
                print(f"⚠️  Gemini failed: {e}. Using fallback...")
        
        # Final fallback: Template-based explanation
        return self._fallback_explanation(solution, conflict, train_a_info, train_b_info)
    
    def _build_context(self, solution: Dict, conflict: Dict,
                      train_a_info: Dict, train_b_info: Dict,
                      alternative_solutions: Optional[List[Dict]] = None) -> str:
        """Build context string for LLM"""
        
        context = f"""You are a Senior Railway Operations Analyst for Indian Railways, explaining decisions to Section Controllers.

SITUATION:
- Train A: {train_a_info['name']} (Priority {train_a_info['priority']}, {train_a_info['mass_tons']:.0f} tons)
  Position: {train_a_info['position_km']:.1f} km, Speed: {train_a_info['speed_kmh']:.0f} km/h
  
- Train B: {train_b_info['name']} (Priority {train_b_info['priority']}, {train_b_info['mass_tons']:.0f} tons)
  Position: {train_b_info['position_km']:.1f} km, Speed: {train_b_info['speed_kmh']:.0f} km/h

CONFLICT:
- Potential collision at {conflict['conflict_position_km']:.1f} km
- Time to conflict: {conflict['time_to_conflict_minutes']:.1f} minutes
- Severity: {conflict['severity']}

RECOMMENDED SOLUTION:
- Action: {solution['action']}
- Type: {solution['type']}
- Energy consumption: {solution['energy_kwh']:.1f} kWh
- Time delay: {solution['delay_minutes']:.1f} minutes
- Priority violation: {'Yes' if solution['priority_violation'] else 'No'}
"""

        # Add multi-step details if applicable
        if solution.get('is_multi_step', False):
            context += "\nMULTI-STEP EXECUTION PLAN:\n"
            for step in solution['steps']:
                context += f"  Step {step['step']}: {step['action']} ({step['time']})\n"
                context += f"    Reason: {step['reason']}\n"
        
        # Add alternatives if provided
        if alternative_solutions and len(alternative_solutions) > 0:
            context += "\nALTERNATIVES CONSIDERED:\n"
            for i, alt in enumerate(alternative_solutions[:2], 1):  # Show top 2 alternatives
                context += f"  {i}. {alt['action']}: {alt['energy_kwh']:.1f} kWh, {alt['delay_minutes']:.1f} min delay\n"
        
        context += """
TASK:
Explain this decision in 2-3 sentences for a Section Controller. Focus on:
1. WHY this solution was chosen (safety, priority, energy, or time)
2. The energy impact (kWh saved or used)
3. Any trade-offs made

Be professional, precise, and emphasize safety and energy efficiency.
Use Indian Railways terminology where appropriate.
"""
        
        return context
    
    def _call_groq(self, context: str) -> str:
        """Call Groq API (Llama 3)"""
        
        response = self.groq_client.chat.completions.create(
            model="llama-3.1-70b-versatile",  # Fast and capable
            messages=[
                {
                    "role": "system",
                    "content": "You are a Senior Railway Operations Analyst for Indian Railways. Explain decisions clearly and professionally."
                },
                {
                    "role": "user",
                    "content": context
                }
            ],
            temperature=0.3,  # Low temperature for consistent, factual responses
            max_tokens=200,   # Keep explanations concise
            top_p=0.9
        )
        
        explanation = response.choices[0].message.content.strip()
        return explanation
    
    def _call_gemini(self, context: str) -> str:
        """Call Gemini API (fallback)"""
        
        response = self.gemini_model.generate_content(
            context,
            generation_config={
                'temperature': 0.3,
                'max_output_tokens': 200,
                'top_p': 0.9
            }
        )
        
        explanation = response.text.strip()
        return explanation
    
    def _fallback_explanation(self, solution: Dict, conflict: Dict,
                             train_a_info: Dict, train_b_info: Dict) -> str:
        """Generate template-based explanation when LLMs unavailable"""
        
        action = solution['action']
        energy = solution['energy_kwh']
        delay = solution['delay_minutes']
        priority_violation = solution['priority_violation']
        
        # Build explanation based on solution type
        if solution['type'] == 'multi_step':
            explanation = (
                f"Recommended action: {action}. "
                f"This multi-step approach first reduces relative closing speed by slowing both trains, "
                f"then provides permanent resolution by switching to a parallel track. "
                f"Energy consumption: {energy:.1f} kWh with {delay:.1f} minutes delay. "
                f"This respects train priorities and prevents future conflicts on this section."
            )
        
        elif solution['type'] == 'both_slow':
            explanation = (
                f"Recommended action: {action}. "
                f"Slowing both trains reduces the relative closing speed, providing more time for safe resolution. "
                f"This approach uses only {energy:.1f} kWh and causes minimal delay ({delay:.1f} minutes). "
                f"Both trains are affected equally, respecting operational priorities."
            )
        
        elif solution['type'] == 'stop':
            train_affected = solution['train_affected']
            if priority_violation:
                explanation = (
                    f"Recommended action: {action}. "
                    f"While this requires {energy:.1f} kWh and {delay:.1f} minutes delay, "
                    f"it's necessary for safety. Note: This affects a higher-priority train, "
                    f"which should only be done in critical situations."
                )
            else:
                explanation = (
                    f"Recommended action: {action}. "
                    f"Stopping {train_affected} respects train priorities and ensures safety. "
                    f"Energy consumption: {energy:.1f} kWh with {delay:.1f} minutes delay. "
                    f"The higher-priority train continues without interruption."
                )
        
        elif solution['type'] == 'slow':
            explanation = (
                f"Recommended action: {action}. "
                f"This provides a balance between safety and schedule adherence. "
                f"Energy consumption: {energy:.1f} kWh with {delay:.1f} minutes delay. "
                f"The train will resume normal speed after the conflict is resolved."
            )
        
        else:
            explanation = (
                f"Recommended action: {action}. "
                f"Energy consumption: {energy:.1f} kWh, delay: {delay:.1f} minutes. "
                f"This solution balances safety, priority, and energy efficiency."
            )
        
        return explanation
    
    def explain_comparison(self, solution_a: Dict, solution_b: Dict) -> str:
        """
        Explain why one solution is better than another.
        
        Args:
            solution_a: First solution
            solution_b: Second solution
            
        Returns:
            Comparison explanation
        """
        
        energy_diff = abs(solution_a['energy_kwh'] - solution_b['energy_kwh'])
        delay_diff = abs(solution_a['delay_minutes'] - solution_b['delay_minutes'])
        
        better = solution_a if solution_a['score'] < solution_b['score'] else solution_b
        worse = solution_b if better == solution_a else solution_a
        
        context = f"""Compare these two railway conflict resolution options:

OPTION A: {solution_a['action']}
- Energy: {solution_a['energy_kwh']:.1f} kWh
- Delay: {solution_a['delay_minutes']:.1f} minutes
- Priority violation: {'Yes' if solution_a['priority_violation'] else 'No'}
- Score: {solution_a['score']:.1f}

OPTION B: {solution_b['action']}
- Energy: {solution_b['energy_kwh']:.1f} kWh
- Delay: {solution_b['delay_minutes']:.1f} minutes
- Priority violation: {'Yes' if solution_b['priority_violation'] else 'No'}
- Score: {solution_b['score']:.1f}

Explain in 1-2 sentences why Option {'A' if better == solution_a else 'B'} is better.
Focus on the key differentiator (priority, energy, or delay).
"""
        
        # Try LLM first
        if self.groq_available:
            try:
                return self._call_groq(context)
            except:
                pass
        
        if self.gemini_available:
            try:
                return self._call_gemini(context)
            except:
                pass
        
        # Fallback
        reasons = []
        if better['priority_violation'] != worse['priority_violation']:
            reasons.append("respects train priorities")
        if energy_diff > 10:
            reasons.append(f"saves {energy_diff:.1f} kWh")
        if delay_diff > 1:
            reasons.append(f"reduces delay by {delay_diff:.1f} minutes")
        
        reason_text = " and ".join(reasons) if reasons else "provides better overall balance"
        
        return f"Option {'A' if better == solution_a else 'B'} is recommended because it {reason_text}."
    
    def generate_summary(self, conflict: Dict, solution: Dict, 
                        energy_saved: Optional[float] = None) -> str:
        """
        Generate executive summary for SC dashboard.
        
        Args:
            conflict: Conflict information
            solution: Chosen solution
            energy_saved: Energy saved compared to worst option (optional)
            
        Returns:
            Executive summary string
        """
        
        summary = f"""CONFLICT RESOLUTION SUMMARY

Situation: Potential collision at {conflict['conflict_position_km']:.1f} km in {conflict['time_to_conflict_minutes']:.1f} minutes

Recommended Action: {solution['action']}

Impact:
- Energy: {solution['energy_kwh']:.1f} kWh
- Delay: {solution['delay_minutes']:.1f} minutes
- Safety: Ensured
- Priority: {'Respected' if not solution['priority_violation'] else 'Override required'}
"""
        
        if energy_saved:
            summary += f"\nEnergy Savings: {energy_saved:.1f} kWh compared to alternative approach"
        
        if solution.get('is_multi_step', False):
            summary += f"\n\nExecution: Multi-step approach with {len(solution['steps'])} phases"
        
        return summary
