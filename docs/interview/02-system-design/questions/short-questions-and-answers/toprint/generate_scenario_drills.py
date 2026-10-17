from __future__ import annotations

import re
from dataclasses import dataclass
from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.pdfbase.pdfmetrics import stringWidth
from reportlab.platypus import (
    BaseDocTemplate,
    Frame,
    KeepTogether,
    PageTemplate,
    Paragraph,
    Spacer,
    Table,
    TableStyle,
)


ROOT = Path(__file__).resolve().parent
SOURCE = ROOT / "system-design-scenario-drills.md"
OUTPUT = ROOT / "system-design-scenario-drills.pdf"

NAVY = colors.HexColor("#16324F")
BLUE = colors.HexColor("#1F5D8F")
PALE_BLUE = colors.HexColor("#EAF2F8")
MID_GRAY = colors.HexColor("#65727E")
LIGHT_GRAY = colors.HexColor("#D9E0E6")
DARK = colors.HexColor("#17212B")


@dataclass(frozen=True)
class ScenarioDrill:
    drill_id: str
    category: str
    title: str
    scenario: str
    question: str
    answer: str


def strip_field(line: str, field: str) -> str:
    prefix = f"**{field}:**"
    if not line.startswith(prefix):
        raise ValueError(f"Expected {prefix}, got: {line}")
    value = line[len(prefix) :].strip()
    if field == "Interview question" and value.startswith('"') and value.endswith('"'):
        value = value[1:-1]
    return value


def parse_source() -> list[ScenarioDrill]:
    category = ""
    drill_id = ""
    title = ""
    scenario = ""
    question = ""
    answer = ""
    drills: list[ScenarioDrill] = []

    def flush() -> None:
        nonlocal drill_id, title, scenario, question, answer
        if not drill_id:
            return
        if not all((category, title, scenario, question, answer)):
            raise ValueError(f"Incomplete drill: {drill_id}")
        drills.append(ScenarioDrill(drill_id, category, title, scenario, question, answer))
        drill_id = ""
        title = ""
        scenario = ""
        question = ""
        answer = ""

    for raw_line in SOURCE.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if raw_line.startswith("## "):
            flush()
            category = raw_line[3:].strip()
        elif raw_line.startswith("### S"):
            flush()
            match = re.match(r"^###\s+(S\d{3})\s+-\s+(.+)$", raw_line)
            if not match:
                raise ValueError(f"Invalid drill heading: {raw_line}")
            drill_id, title = match.groups()
        elif drill_id and line.startswith("**Scenario:**"):
            scenario = strip_field(line, "Scenario")
        elif drill_id and line.startswith("**Interview question:**"):
            question = strip_field(line, "Interview question")
        elif drill_id and line.startswith("**Answer:**"):
            answer = strip_field(line, "Answer")
        elif drill_id and line:
            raise ValueError(f"Unexpected content in {drill_id}: {raw_line}")
    flush()

    expected_ids = [f"S{index:03d}" for index in range(1, 101)]
    actual_ids = [drill.drill_id for drill in drills]
    if actual_ids != expected_ids:
        raise ValueError(f"Expected sequential S001-S100, got {actual_ids[:3]}...{actual_ids[-3:]}")
    if len(dict.fromkeys(drill.category for drill in drills)) != 10:
        raise ValueError("Expected exactly 10 scenario categories")
    return drills


def styles() -> dict[str, ParagraphStyle]:
    base = getSampleStyleSheet()
    return {
        "title": ParagraphStyle(
            "ScenarioTitle",
            parent=base["Title"],
            fontName="Helvetica-Bold",
            fontSize=14.5,
            leading=16.5,
            textColor=NAVY,
            alignment=TA_CENTER,
            spaceAfter=2.3 * mm,
        ),
        "intro": ParagraphStyle(
            "ScenarioIntro",
            parent=base["BodyText"],
            fontName="Helvetica",
            fontSize=7.0,
            leading=8.25,
            textColor=MID_GRAY,
            alignment=TA_LEFT,
            spaceAfter=2.2 * mm,
        ),
        "category": ParagraphStyle(
            "ScenarioCategory",
            parent=base["Heading2"],
            fontName="Helvetica-Bold",
            fontSize=8.8,
            leading=10.2,
            textColor=colors.white,
            backColor=NAVY,
            borderPadding=(2.1, 3.2, 2.1, 3.2),
            spaceBefore=1.2 * mm,
            spaceAfter=1.2 * mm,
            keepWithNext=True,
        ),
        "drill_title": ParagraphStyle(
            "DrillTitle",
            parent=base["BodyText"],
            fontName="Helvetica-Bold",
            fontSize=7.25,
            leading=8.3,
            textColor=BLUE,
            spaceAfter=0.45 * mm,
        ),
        "scenario": ParagraphStyle(
            "ScenarioBody",
            parent=base["BodyText"],
            fontName="Helvetica",
            fontSize=6.75,
            leading=7.8,
            textColor=DARK,
            spaceAfter=0.4 * mm,
        ),
        "question": ParagraphStyle(
            "InterviewQuestion",
            parent=base["BodyText"],
            fontName="Helvetica-BoldOblique",
            fontSize=6.85,
            leading=7.95,
            textColor=NAVY,
            backColor=PALE_BLUE,
            borderPadding=(1.5, 2.2, 1.5, 2.2),
            spaceAfter=0.5 * mm,
        ),
        "answer": ParagraphStyle(
            "ScenarioAnswer",
            parent=base["BodyText"],
            fontName="Helvetica",
            fontSize=6.65,
            leading=7.75,
            textColor=DARK,
            spaceAfter=1.45 * mm,
        ),
        "review_note": ParagraphStyle(
            "ReviewNote",
            parent=base["BodyText"],
            fontName="Helvetica",
            fontSize=6.8,
            leading=8.2,
            textColor=DARK,
            spaceAfter=1.5 * mm,
        ),
        "review_cell": ParagraphStyle(
            "ReviewCell",
            parent=base["BodyText"],
            fontName="Helvetica",
            fontSize=6.2,
            leading=7.2,
            textColor=DARK,
        ),
        "review_header": ParagraphStyle(
            "ReviewHeader",
            parent=base["BodyText"],
            fontName="Helvetica-Bold",
            fontSize=6.2,
            leading=7.2,
            textColor=colors.white,
        ),
    }


def draw_page(canvas, doc) -> None:
    canvas.saveState()
    width, height = A4
    canvas.setStrokeColor(LIGHT_GRAY)
    canvas.setLineWidth(0.4)
    canvas.line(width / 2, 11 * mm, width / 2, height - 10 * mm)
    canvas.setFont("Helvetica", 6.4)
    canvas.setFillColor(MID_GRAY)
    canvas.drawString(8.5 * mm, 6.5 * mm, "System Design Scenario Drills - 100 Interview Questions")
    page_text = f"Page {doc.page}"
    canvas.drawString(width - 8.5 * mm - stringWidth(page_text, "Helvetica", 6.4), 6.5 * mm, page_text)
    canvas.restoreState()


def build_pdf(drills: list[ScenarioDrill]) -> None:
    width, height = A4
    margin_x = 8.5 * mm
    margin_top = 9 * mm
    margin_bottom = 11 * mm
    gap = 6 * mm
    column_width = (width - (2 * margin_x) - gap) / 2
    frame_height = height - margin_top - margin_bottom
    frames = [
        Frame(margin_x, margin_bottom, column_width, frame_height, leftPadding=0, rightPadding=0),
        Frame(margin_x + column_width + gap, margin_bottom, column_width, frame_height, leftPadding=0, rightPadding=0),
    ]

    document = BaseDocTemplate(
        str(OUTPUT),
        pagesize=A4,
        leftMargin=margin_x,
        rightMargin=margin_x,
        topMargin=margin_top,
        bottomMargin=margin_bottom,
        title="System Design Scenario Drills - 100 Interview Questions",
        author="Instagram Backend Learning Project",
    )
    document.addPageTemplates(PageTemplate(id="TwoColumn", frames=frames, onPage=draw_page))
    page_styles = styles()
    story = [
        Paragraph("SYSTEM DESIGN SCENARIO DRILLS", page_styles["title"]),
        Paragraph(
            "Read only the scenario and interview question. Speak for 60-90 seconds, then compare the answer. Mark the three boxes after a first correct answer, a next-day recall, and a seven-day recall. Prefer evidence and containment before confident guesses.",
            page_styles["intro"],
        ),
    ]

    active_category = None
    for drill in drills:
        if drill.category != active_category:
            active_category = drill.category
            story.append(Paragraph(active_category, page_styles["category"]))
        story.append(
            KeepTogether(
                [
                    Paragraph(
                        f"<b>{drill.drill_id}</b>  {drill.title} &nbsp;&nbsp; [ ] [ ] [ ]",
                        page_styles["drill_title"],
                    ),
                    Paragraph(f"<b>Situation:</b> {drill.scenario}", page_styles["scenario"]),
                    Paragraph(f'"{drill.question}"', page_styles["question"]),
                    Paragraph(f"<b>Answer:</b> {drill.answer}", page_styles["answer"]),
                ]
            )
        )
        story.append(Spacer(1, 0.15 * mm))

    answer_shape = Paragraph(
        "<b>1.</b> State the leading hypothesis without claiming certainty. "
        "<b>2.</b> Name the first metrics, traces, or logs to compare. "
        "<b>3.</b> Contain customer impact before deep investigation. "
        "<b>4.</b> Isolate the bottleneck or failed boundary. "
        "<b>5.</b> Finish with the durable fix, trade-off, and prevention signal.",
        page_styles["review_note"],
    )
    review_rows = [
        [
            Paragraph("Date", page_styles["review_header"]),
            Paragraph("Drill IDs", page_styles["review_header"]),
            Paragraph("Weak spot / repair", page_styles["review_header"]),
            Paragraph("/5", page_styles["review_header"]),
        ]
    ]
    review_rows.extend([["", "", "", ""] for _ in range(15)])
    review_table = Table(
        review_rows,
        colWidths=[18 * mm, 25 * mm, 43 * mm, 9 * mm],
        rowHeights=[5.5 * mm] + [9.5 * mm] * 15,
        repeatRows=1,
    )
    review_table.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, 0), NAVY),
                ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
                ("GRID", (0, 0), (-1, -1), 0.35, LIGHT_GRAY),
                ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, PALE_BLUE]),
                ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
                ("LEFTPADDING", (0, 0), (-1, -1), 2),
                ("RIGHTPADDING", (0, 0), (-1, -1), 2),
                ("TOPPADDING", (0, 0), (-1, -1), 1.5),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 1.5),
            ]
        )
    )
    story.append(
        KeepTogether(
            [
                Paragraph("60-SECOND ANSWER SHAPE", page_styles["category"]),
                answer_shape,
                Paragraph("MORNING SCENARIO REVIEW LOG", page_styles["category"]),
                Paragraph(
                    "Record the scenarios you attempted and the part of your answer that needs repair before the next recall.",
                    page_styles["review_note"],
                ),
                review_table,
            ]
        )
    )
    document.build(story)


def main() -> None:
    drills = parse_source()
    build_pdf(drills)
    print(f"Generated {OUTPUT.name}: {len(drills)} scenario drills")


if __name__ == "__main__":
    main()
