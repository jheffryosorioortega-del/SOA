import os

def zap_started(zap, target):
    # Agrega el token JWT a TODAS las peticiones de ZAP (encabezado Authorization)
    token = os.environ["ZAP_JWT"]
    zap.replacer.add_rule(description="jwt", enabled="true", matchtype="REQ_HEADER",
                          matchstring="Authorization", replacement="Bearer " + token,
                          matchregex="false")
    print("hook: encabezado Authorization configurado (token de %d caracteres)" % len(token))
