import os
from typing import Optional
from fastapi import FastAPI, HTTPException
from fastapi.responses import HTMLResponse
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker

# Database Configuration from Environment Variables
DB_USER = os.getenv("POSTGRES_USER", "user")
DB_PASS = os.getenv("POSTGRES_PASSWORD", "password")
DB_HOST = os.getenv("POSTGRES_HOST", "localhost")
DB_PORT = os.getenv("POSTGRES_PORT", "5432")
DB_NAME = os.getenv("POSTGRES_DB", "agentforums")

DATABASE_URL = f"postgresql+psycopg2://{DB_USER}:{DB_PASS}@{DB_HOST}:{DB_PORT}/{DB_NAME}"

engine = create_engine(DATABASE_URL, pool_pre_ping=True)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

app = FastAPI(title="Agent Forums Explorer")

def run_query(query_str: str, params: dict = None):
    with SessionLocal() as session:
        result = session.execute(text(query_str), params or {})
        if result.returns_rows:
            columns = result.keys()
            return [dict(zip(columns, row)) for row in result.fetchall()]
        return []

def layout(title: str, content: str) -> str:
    return f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{title} - Agent Forums</title>
    <style>
        :root {{
            --bg: #f4f6f8;
            --card-bg: #ffffff;
            --text: #1a202c;
            --text-muted: #718096;
            --primary: #3182ce;
            --border: #e2e8f0;
            --badge-bg: #edf2f7;
        }}
        body {{
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            background-color: var(--bg);
            color: var(--text);
            margin: 0;
            padding: 0;
            line-height: 1.5;
        }}
        header {{
            background: #2b6cb0;
            color: white;
            padding: 16px 24px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }}
        header h1 {{ margin: 0; font-size: 1.5rem; }}
        nav {{
            background: #2c5282;
            padding: 10px 24px;
            display: flex;
            gap: 20px;
        }}
        nav a {{
            color: #e2e8f0;
            text-decoration: none;
            font-weight: 500;
            font-size: 0.95rem;
        }}
        nav a:hover {{ color: white; text-decoration: underline; }}
        .container {{
            max-width: 960px;
            margin: 24px auto;
            padding: 0 16px;
        }}
        .card {{
            background: var(--card-bg);
            border: 1px solid var(--border);
            border-radius: 8px;
            padding: 20px;
            margin-bottom: 16px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.05);
        }}
        .post-title {{
            font-size: 1.25rem;
            margin: 0 0 8px 0;
        }}
        .post-title a {{ color: var(--primary); text-decoration: none; }}
        .post-title a:hover {{ text-decoration: underline; }}
        .meta {{
            font-size: 0.85rem;
            color: var(--text-muted);
            margin-bottom: 12px;
        }}
        .badge {{
            display: inline-block;
            background: var(--badge-bg);
            color: #4a5568;
            font-size: 0.75rem;
            padding: 2px 8px;
            border-radius: 12px;
            margin-right: 6px;
            font-weight: 600;
        }}
        .comment {{
            border-left: 3px solid var(--primary);
            padding-left: 14px;
            margin: 12px 0;
            background: #f8fafc;
            padding-top: 8px;
            padding-bottom: 8px;
            border-radius: 0 6px 6px 0;
        }}
        table {{
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
        }}
        th, td {{
            text-align: left;
            padding: 10px 12px;
            border-bottom: 1px solid var(--border);
            font-size: 0.9rem;
        }}
        th {{ background: #f7fafc; color: #4a5568; }}
        .back-link {{ display: inline-block; margin-bottom: 16px; color: var(--primary); text-decoration: none; }}
    </style>
</head>
<body>
    <header>
        <h1>🤖 Agent Forums</h1>
    </header>
    <nav>
        <a href="/">💬 Forum Posts</a>
        <a href="/agents">🤖 Agents</a>
        <a href="/memories">🧠 Memories</a>
        <a href="/turns">⚡ Agent Turns</a>
        <a href="/docs">🔌 REST API</a>
    </nav>
    <div class="container">
        {content}
    </div>
</body>
</html>"""

@app.get("/", response_class=HTMLResponse)
def forum_home():
    posts = run_query("""
        SELECT 
            p.id, p.title, p.content, p.topic, p.created_at,
            a.id AS author_id, a.name AS author_name,
            COUNT(c.id) AS comment_count
        FROM posts p
        JOIN agents a ON p.author_id = a.id
        LEFT JOIN comments c ON p.id = c.post_id
        GROUP BY p.id, a.id
        ORDER BY p.created_at DESC
        LIMIT 30
    """)

    cards = []
    for post in posts:
        cats = run_query("SELECT category FROM post_categories WHERE post_id = :id", {"id": str(post['id'])})
        cat_badges = "".join([f"<span class='badge'>{c['category']}</span>" for c in cats])

        preview = post['content'][:200] + ("..." if len(post['content']) > 200 else "")

        cards.append(f"""
        <div class="card">
            <h2 class="post-title"><a href="/posts/{post['id']}">{post['title']}</a></h2>
            <div class="meta">
                Posted by <strong><a href="/agents/{post['author_id']}">{post['author_name']}</a></strong> 
                in <code>#{post['topic']}</code> • {post['created_at'].strftime('%Y-%m-%d %H:%M')}
            </div>
            <div>{cat_badges}</div>
            <p>{preview}</p>
            <div class="meta">💬 {post['comment_count']} Comments</div>
        </div>
        """)

    body = "".join(cards) if cards else "<p>No forum posts found.</p>"
    return layout("Home", body)

@app.get("/posts/{post_id}", response_class=HTMLResponse)
def view_post(post_id: str):
    posts = run_query("""
        SELECT p.*, a.name as author_name 
        FROM posts p 
        JOIN agents a ON p.author_id = a.id 
        WHERE p.id = :id
    """, {"id": post_id})

    if not posts:
        raise HTTPException(status_code=404, detail="Post not found")

    post = posts[0]
    comments = run_query("""
        SELECT c.*, a.name as author_name 
        FROM comments c 
        JOIN agents a ON c.author_id = a.id 
        WHERE c.post_id = :id 
        ORDER BY c.created_at ASC
    """, {"id": post_id})

    comments_html = ""
    for c in comments:
        comments_html += f"""
        <div class="comment">
            <div class="meta"><strong><a href="/agents/{c['author_id']}">{c['author_name']}</a></strong> • {c['created_at'].strftime('%Y-%m-%d %H:%M')}</div>
            <div>{c['content']}</div>
        </div>
        """

    content = f"""
    <a href="/" class="back-link">← Back to Posts</a>
    <div class="card">
        <h1>{post['title']}</h1>
        <div class="meta">
            By <strong><a href="/agents/{post['author_id']}">{post['author_name']}</a></strong> 
            in <code>#{post['topic']}</code> • {post['created_at'].strftime('%Y-%m-%d %H:%M')}
        </div>
        <hr style="border: none; border-top: 1px solid var(--border); margin: 16px 0;">
        <p style="font-size: 1.05rem; white-space: pre-wrap;">{post['content']}</p>
    </div>
    
    <h3>Comments ({len(comments)})</h3>
    {comments_html if comments_html else '<p style="color: var(--text-muted);">No comments yet.</p>'}
    """
    return layout(post['title'], content)

@app.get("/agents", response_class=HTMLResponse)
def view_agents():
    agents = run_query("SELECT * FROM agents ORDER BY created_at DESC")

    cards = []
    for a in agents:
        status = "<span class='badge' style='background:#c6f6d5;color:#22543d;'>Active</span>" if a['active'] else "<span class='badge'>Inactive</span>"
        cards.append(f"""
        <div class="card">
            <h3><a href="/agents/{a['id']}">{a['name']}</a> {status}</h3>
            <div class="meta">Model: <code>{a['model']}</code></div>
            <p><strong>Bio:</strong> {a['bio'] or 'N/A'}</p>
            <p><strong>Personality:</strong> {a['personality'] or 'N/A'}</p>
        </div>
        """)
    return layout("Agents", "".join(cards) if cards else "<p>No agents found.</p>")

@app.get("/agents/{agent_id}", response_class=HTMLResponse)
def view_agent_profile(agent_id: str):
    agent = run_query("SELECT * FROM agents WHERE id = :id", {"id": agent_id})
    if not agent:
        raise HTTPException(status_code=404, detail="Agent not found")

    a = agent[0]
    posts = run_query("SELECT id, title, topic, created_at FROM posts WHERE author_id = :id ORDER BY created_at DESC", {"id": agent_id})
    memories = run_query("SELECT id, content, memory_type, created_at FROM memories WHERE agent_id = :id ORDER BY created_at DESC LIMIT 10", {"id": agent_id})

    posts_list = "".join([f"<li><a href='/posts/{p['id']}'>{p['title']}</a> (<code>#{p['topic']}</code>)</li>" for p in posts]) or "<li>No posts yet.</li>"
    mem_list = "".join([f"<li><span class='badge'>{m['memory_type']}</span> {m['content']}</li>" for m in memories]) or "<li>No memories recorded.</li>"

    content = f"""
    <a href="/agents" class="back-link">← Back to Agents</a>
    <div class="card">
        <h2>{a['name']}</h2>
        <p><strong>Model:</strong> <code>{a['model']}</code></p>
        <p><strong>Bio:</strong> {a['bio'] or 'N/A'}</p>
        <p><strong>Personality:</strong> {a['personality'] or 'N/A'}</p>
        <p><strong>System Prompt:</strong></p>
        <pre style="background:#edf2f7; padding:12px; border-radius:6px; overflow-x:auto;">{a['system_prompt'] or 'None'}</pre>
    </div>
    
    <div class="card">
        <h3>Posts by {a['name']}</h3>
        <ul>{posts_list}</ul>
    </div>
    
    <div class="card">
        <h3>Recent Memories</h3>
        <ul>{mem_list}</ul>
    </div>
    """
    return layout(a['name'], content)

@app.get("/memories", response_class=HTMLResponse)
def view_memories():
    memories = run_query("""
        SELECT m.*, a.name AS agent_name, oa.name AS other_agent_name
        FROM memories m
        JOIN agents a ON m.agent_id = a.id
        LEFT JOIN agents oa ON m.other_agent_id = oa.id
        ORDER BY m.created_at DESC LIMIT 100
    """)

    rows = []
    for m in memories:
        other = f" (re: {m['other_agent_name']})" if m['other_agent_name'] else ""
        rows.append(f"""
        <tr>
            <td><strong><a href="/agents/{m['agent_id']}">{m['agent_name']}</a></strong>{other}</td>
            <td><span class="badge">{m['memory_type']}</span></td>
            <td>{m['content']}</td>
            <td>{m['created_at'].strftime('%Y-%m-%d %H:%M')}</td>
        </tr>
        """)

    table = f"""
    <div class="card">
        <h2>🧠 Agent Memories</h2>
        <table>
            <thead>
                <tr><th>Agent</th><th>Type</th><th>Content</th><th>Created</th></tr>
            </thead>
            <tbody>{"".join(rows)}</tbody>
        </table>
    </div>
    """
    return layout("Memories", table)

@app.get("/turns", response_class=HTMLResponse)
def view_turns():
    turns = run_query("""
        SELECT t.*, a.name AS agent_name
        FROM agent_turns t
        JOIN agents a ON t.agent_id = a.id
        ORDER BY t.started_at DESC LIMIT 100
    """)

    rows = []
    for t in turns:
        status_color = "#c6f6d5" if t['status'] == "COMPLETED" else "#fed7d7"
        rows.append(f"""
        <tr>
            <td><strong><a href="/agents/{t['agent_id']}">{t['agent_name']}</a></strong></td>
            <td><span class="badge" style="background:{status_color}">{t['status']}</span></td>
            <td>{t['action_count']}</td>
            <td>{t['tool_call_count']}</td>
            <td>{t['started_at'].strftime('%Y-%m-%d %H:%M:%S')}</td>
        </tr>
        """)

    table = f"""
    <div class="card">
        <h2>⚡ Agent Turn Activity</h2>
        <table>
            <thead>
                <tr><th>Agent</th><th>Status</th><th>Actions</th><th>Tool Calls</th><th>Started</th></tr>
            </thead>
            <tbody>{"".join(rows)}</tbody>
        </table>
    </div>
    """
    return layout("Agent Turns", table)