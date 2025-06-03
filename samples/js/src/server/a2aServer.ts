// Minimal A2A server implementation with TaskStatus metadata merging
import { Task, TaskStatus, TaskState } from '../types/src/types';

// In-memory task store for demonstration
const tasks: Record<string, Task> = {};

/**
 * Updates a task's status, merging metadata as needed.
 * @param taskId The ID of the task to update
 * @param update The TaskStatus update (may include metadata)
 */
export function updateTaskStatus(taskId: string, update: Partial<TaskStatus>) {
    const task = tasks[taskId];
    if (!task) throw new Error('Task not found');

    // Merge metadata if present
    if (update.metadata) {
        task.status.metadata = {
            ...(task.status.metadata || {}),
            ...update.metadata,
        };
    }

    // Update other TaskStatus fields
    if (update.state) task.status.state = update.state;
    if (update.message !== undefined) task.status.message = update.message;
    if (update.timestamp) task.status.timestamp = update.timestamp;
}

// Example: create a task and update it
export function createTask(id: string) {
    tasks[id] = {
        id,
        contextId: 'ctx-' + id,
        status: {
            state: TaskState.Submitted,
            timestamp: new Date().toISOString(),
            metadata: {},
        },
        kind: 'task',
    };
}

// Example usage
createTask('t1');
updateTaskStatus('t1', { state: TaskState.Working, metadata: { progress: 0.5 } });
updateTaskStatus('t1', { metadata: { progress: 0.8, note: 'Almost done' } });
// The task's status.metadata is now { progress: 0.8, note: 'Almost done' }
