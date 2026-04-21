import os
import glob

def fix_encoding(text):
    try:
        # The text was likely read as cp1252 when it was actually utf-8
        # Then saved as utf-8. Let's reverse it.
        # However, some characters might not map perfectly, so we can also fallback to manual replacements if needed.
        return text.encode('windows-1252').decode('utf-8')
    except Exception as e:
        # Fallback to manual replacements if encode/decode fails
        replacements = {
            'Ã¡': 'á', 'Ã©': 'é', 'Ã\xad': 'í', 'Ã³': 'ó', 'Ãº': 'ú', 'Ã±': 'ñ',
            'Ã\x81': 'Á', 'Ã\x89': 'É', 'Ã\x8d': 'Í', 'Ã\x93': 'Ó', 'Ã\x9a': 'Ú', 'Ã\x91': 'Ñ',
            'Ã ': 'Á', 'Ã‰': 'É', 'Ã ': 'Í', 'Ã“': 'Ó', 'Ãš': 'Ú', 'Ã‘': 'Ñ',
            'Âº': 'º', 'Âª': 'ª', 'BÃ SICO': 'BÁSICO', 'Ã NDICE': 'ÍNDICE', 'NÃšMERO': 'NÚMERO',
            'AÃ‘O': 'AÑO', 'aÃ±o': 'año', 'INSTALACIÃ“N': 'INSTALACIÓN',
            'justificaciÃ³n': 'justificación', 'JustificaciÃ³n': 'Justificación',
            'PÃ¡gina': 'Página', 'pÃ¡gina': 'página'
        }
        for bad, good in replacements.items():
            text = text.replace(bad, good)
        return text

# Test with known bad strings from the file
bad_strings = [
    "Estudio BÃ¡sico de Seguridad y Salud",
    "ESTUDIO BÃ SICO DE SEGURIDAD Y SALUD",
    "INSTALACIÃ“N DE AUTOCONSUMO FOTOVOLTAICO",
    "resultando un pico mÃ¡ximo de diversos oficios",
    "sistema de aproximaciÃ³n cada 15 dÃ­as",
    "volumen medio de personal de la categorÃ­a",
    "NÃšMERO DE TRABAJADORES",
    "1.10. JUSTIFICACIÃ“N ESTUDIO BÃ SICO DE SEGURIDAD Y SALUD",
    "Ã NDICE"
]

for s in bad_strings:
    try:
        fixed = s.encode('windows-1252').decode('utf-8')
        print(f"Auto-fix: {s} -> {fixed}")
    except Exception as e:
        print(f"Auto-fix failed for {s}: {e}")
        fixed_manual = fix_encoding(s)
        print(f"Manual-fix: {s} -> {fixed_manual}")
