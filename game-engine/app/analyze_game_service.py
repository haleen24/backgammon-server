import os
import shutil
import subprocess


def analyze(request):
    path = str(request["matchId"]) + ".sgf"
    if os.path.exists(path):
        os.remove(path)
    for game in request["games"]:
        convert_game_and_write(game, path)
    analyze_path = path + ".txt"
    engine_analyze(path, analyze_path)
    return read_analysis(analyze_path, len(request["games"]))


def convert_game_and_write(request, path):
    items = request["items"]
    turn = "B" if request["firstToMove"] == "BLACK" else "W"
    end_game_event = items[-1]
    if end_game_event["type"] == "GAME_END":
        ws = end_game_event["white"]
        bs = end_game_event["black"]
        length = request["thresholdPoints"]
        items = items[:-1]
    else:
        ws = 0
        bs = 0
        length = 3
    game_id = request["gameId"]
    with open(path, 'a', encoding="utf-8") as file:
        file.write(
            f"(;FF[4]GM[6]AP[GNU Backgammon]MI[length:{length}][game:{game_id}][ws:{ws}][bs:{bs}]PW[gnubg]PB[Admin]DT[2025-04-30]\n")
        for item in items:
            file.write(";")
            item_type = item["type"]
            if item_type == "MOVE":
                dice = item["dice"]
                moves = item["moves"]
                file.write(f"{turn}[{dice[0]}{dice[1]}{convert_moves_to_sgf_notation(moves)}]\n")
                turn = "B" if turn == "W" else "W"
            elif item_type == "OFFER_DOUBLE":
                file.write(f"{turn}[double]\n")
                turn = "B" if turn == "W" else "W"
            elif item_type == "ACCEPT_DOUBLE":
                file.write(f"{turn}[take]\n")
                turn = "B" if turn == "W" else "W"
        file.write(")\n")


def engine_analyze(game_path, analyze_path):
    ab_analyze_path = os.path.join(os.getcwd(), analyze_path)
    ab_game_path = os.path.join(os.getcwd(), game_path)
    gnubg_commands = f"""
    load match {ab_game_path}
    analyze match 
    export match text {ab_analyze_path}
    quit
    """
    # Запускаем gnubg как подпроцесс с передачей команд через stdin
    gnubg_path = shutil.which("gnubg")
    process = subprocess.Popen(
        [gnubg_path],
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        shell=True,
        text=True
    )

    # Посылаем команды и ждем завершения
    stdout, stderr = process.communicate(gnubg_commands)

    # Проверяем результат
    if process.returncode != 0:
        print(f"Ошибка при выполнении gnubg: {stderr}")
        return

    print(f"Анализ сохранен в {analyze_path}")


def convert_moves_to_sgf_notation(moves):
    def convert(x):
        return chr(x + ord('a') - 1)

    chars = []
    for move in moves:
        move_from = move["from"]
        if move_from == 0 or move_from == 25:
            move_from = ord('y') + 1 - ord('a')
        move_to = move["to"]
        if move_to == 0 or move_to == 25:
            move_to = ord('z') + 1 - ord('a')
        chars.append(f"{convert(move_from)}{convert(move_to)}")
    return "".join(chars)


def split_without_empty(line):
    return [i for i in line.split("  ") if i]


def read_analysis(path: str, games_count):
    current_game = 0
    data_by_game = [{"items": [], "overall": []}] * games_count
    overall_match = []
    move_data = None
    stage = None
    with open(path, 'r', encoding='utf-8') as file:
        for line in file:
            line = line.strip()
            if len(line) == 0:
                continue
            if line.startswith("Move number"):
                if move_data:
                    data_by_game[current_game]["items"].append(move_data)
                move_data = {
                    "move": None,
                    "rolled": None,
                    "best_moves": [],
                    "alerts": [],
                    "cube": []
                }
                stage = "READ_MOVE"
            if stage == "READ_MOVE" and line.startswith("*"):
                move_data["move"] = line
            if stage == "READ_MOVE" and line.startswith("Alert:"):
                move_data["alerts"].append(line[:-1])

            if line.startswith("Cube analysis"):
                stage = "CUBE_ANALYSIS"
            if stage == "CUBE_ANALYSIS":
                move_data["cube"].append(line)

            if line.startswith("Rolled"):
                move_data["rolled"] = line
                stage = "ROLLED"
            if stage == "ROLLED" and (line[0] == '*' or line[1] == '.'):
                move_data["best_moves"].append(line)

            if line.startswith("Game statistics"):
                data_by_game[current_game]["items"].append(move_data)
                move_data = None
                current_game += 1
                stage = "GAME_STATISTICS"
            elif stage == "GAME_STATISTICS":
                data_by_game[current_game - 1]["overall"].append(line)

            if line.startswith("Match statistics"):
                stage = "MATCH_STATISTICS"
            elif stage == "MATCH_STATISTICS":
                overall_match.append(line)

    return {"games": data_by_game, "overall": overall_match}
